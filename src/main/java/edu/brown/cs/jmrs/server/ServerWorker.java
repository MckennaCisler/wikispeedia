package edu.brown.cs.jmrs.server;

import java.net.HttpCookie;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiFunction;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.collect.ConcurrentBiMap;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.server.threading.MessageQueue;
import edu.brown.cs.jmrs.ui.Main;

/**
 * Main workhorse class for the server/backend. Manages both clients and
 * lobbies.
 *
 * @author shastin1
 *
 */
class ServerWorker {

  private Server                           server;
  private LobbyManager                     lobbies;
  private ConcurrentBiMap<Session, Client> clients;
  private Map<String, Client>              notInLobbies;
  private Queue<Client>                    disconnectedClients;
  private Gson                             gson;
  private MessageQueue                     messageQueue;

  /**
   * Constructor specifying Server instance, lobby factory, and Gson instance to
   * use.
   *
   * @param server
   *          Server instance that "users" (of the library) see and interact
   *          with
   * @param lobbyFactory
   *          Factory for instantiating lobbies on command
   * @param gson
   *          Gson instance for JSONification of lobbies
   */
  public ServerWorker(
      Server server,
      BiFunction<Server, String, ? extends Lobby> lobbyFactory,
      Gson gson) {
    this.server = server;
    lobbies = new LobbyManager(lobbyFactory);
    clients = new ConcurrentBiMap<>();
    notInLobbies = new ConcurrentHashMap<>();
    disconnectedClients = new PriorityBlockingQueue<>();
    this.gson = gson;
    messageQueue = new MessageQueue();
  }

  /**
   * Returns a list of all lobbies as a JsonObject for sending to clients.
   *
   * @return a list of all lobbies as a JsonObject for sending to clients
   */
  private JsonObject allLobbies() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("command", "all_lobbies");
    List<String> lobbs = getOpenLobbies();
    jsonObject.addProperty("error_message", "");

    JsonArray lobbyArray = new JsonArray();
    for (String id : lobbs) {
      lobbyArray.add(getLobby(id).toJson(gson));
    }

    jsonObject.add("payload", lobbyArray);
    return jsonObject;
  }

  /**
   * Checks disconnected but stored players, and if they have expired cookies
   * references to them are removed.
   */
  private void checkDisconnectedPlayers() {
    if (!disconnectedClients.isEmpty()) {
      Date now = new Date();
      Client p = disconnectedClients.poll();
      while (p.getCookieExpiration().before(now)) {
        clients.remove(clients.getReversed(p));
        if (disconnectedClients.isEmpty()) {
          return;
        }
        p = disconnectedClients.poll();
      }
      disconnectedClients.add(p);
    }
  }

  /**
   * Connects given client and sets their id, sends that id to the client, and
   * sends a list of all lobbies to the client.
   *
   * @param conn
   *          The client connection that just connected
   */
  public void clientConnected(Session conn) {
    checkDisconnectedPlayers();

    String clientId = "";
    List<HttpCookie> cookies = conn.getUpgradeRequest().getCookies();
    for (HttpCookie cookie : cookies) {
      if (cookie.getName().equals("client_id")) {
        String cookieString = cookie.getValue();

        clientId = cookieString.substring(0, cookieString.indexOf(":"));
        break;
      }
    }

    // notify client of their id

    String toClient = "";
    String trueId = "";

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("command", "notify_id");
    try {
      trueId = setClientId(conn, clientId);

      jsonObject.addProperty("client_id", trueId);
      jsonObject.addProperty("error_message", "");
      toClient = gson.toJson(jsonObject);
      sendToClient(conn, toClient);

    } catch (InputError e) {
      jsonObject.addProperty("client_id", "");
      jsonObject.addProperty("error_message", e.getMessage());
      toClient = gson.toJson(jsonObject);
      sendToClient(conn, toClient);
    }

    Client client = clients.get(conn);
    if (client != null) {
      // if they are not in a lobby, give them a list of lobbies
      if (client.getLobby() == null) {
        notInLobbies.put(client.getId(), client);
      } else {
        // if they are already in a lobby and thus reconnecting, note that
        // they're reconnecting
        client.getLobby().playerReconnected(client.getId());
      }
    }
    sendLobbies(client);
  }

  /**
   * Removes references to given client, or sets a flag on them identifying them
   * as disconnected if they are in a lobby.
   *
   * @param conn
   *          The client who disconnected
   */
  public void clientDisconnected(Session conn) {
    Date expiration = new Date();
    List<HttpCookie> cookies = conn.getUpgradeRequest().getCookies();
    for (HttpCookie cookie : cookies) {
      if (cookie.getName().equals("client_id")) {
        String cookieVal = cookie.getValue();
        expiration = Date.from(
            Instant.ofEpochMilli(
                Long.parseLong(
                    cookieVal.substring(cookieVal.indexOf(":") + 1))));
        break;
      }
    }

    Client client = clients.get(conn);
    if (client != null && client.getLobby() == null) {
      notInLobbies.remove(client.getId());
    }

    if (expiration.after(new Date())) {
      if (client != null) {
        assert client.isConnected();
        client.toggleConnected();

        client.setCookieExpiration(expiration);
        disconnectedClients.add(client);

        if (client.getLobby() != null) {
          client.getLobby().playerDisconnected(client.getId());
        }
      }
    } else {
      clients.remove(conn);
    }
  }

  /**
   * closes the lobby associated with the given id if it exists
   *
   * @param lobbyId
   *          The id to close the lobby associated with
   */
  public void closeLobby(String lobbyId) {
    lobbies.remove(lobbyId);
  }

  /**
   * Creates a new lobby with given id, or throws error if id is already taken.
   *
   * @param lobbyId
   *          The id to create a lobby associated with
   * @return The lobby created
   * @throws InputError
   *           If the given id is already in use by another lobby
   */
  public Lobby createLobby(String lobbyId) throws InputError {
    Lobby lobby = lobbies.create(lobbyId, server);
    if (lobby == null) {
      throw new InputError("Lobby ID in use");
    } else {
      return lobby;
    }
  }

  /**
   * Returns Client associated with given client connection.
   *
   * @param conn
   *          The client connection who's associated Client is returned
   * @return The Client associated with the given client id
   */
  public Client getClient(Session conn) {
    return clients.get(conn);
  }

  /**
   * Returns the client connection associated with the given client id.
   *
   * @param clientId
   *          The id who's associated client connection is to be returned
   * @return The client connection associated with the given id
   */
  public Session getClient(String clientId) {
    return clients.getReversed(new Client(clientId));
  }

  /**
   * Returns the lobby associated with the given id, or null if no such lobby
   * exists.
   *
   * @param lobbyId
   *          The id to find the lobby associated with
   * @return the lobby associated with the given id, or null if no such lobby
   *         exists
   */
  public Lobby getLobby(String lobbyId) {
    return lobbies.get(lobbyId);
  }

  /**
   * Returns a list of all currently open lobbies.
   *
   * @return a list of all currently open lobbies
   */
  public List<String> getOpenLobbies() {
    return lobbies.getOpenLobbies();
  }

  /**
   * Returns a map of ids to Clients for clients who are not in lobbies.
   *
   * @return a map of ids to Clients for clients who are not in lobbies
   */
  public Map<String, Client> lobbylessMap() {
    return notInLobbies;
  }

  /**
   * Sends up-to-date list of lobbies to given client.
   *
   * @param client
   *          client to send lobbies to
   */
  private void sendLobbies(Client client) {
    if (client != null) {
      sendToClient(clients.getReversed(client), gson.toJson(allLobbies()));
      Main.debugLog("Open lobbies: " + getOpenLobbies());
    }
  }

  /**
   * Sends the given message through the given client connection.
   *
   * @param client
   *          The client connection to send through
   * @param message
   *          The message so send
   */
  public void sendToClient(Session client, String message) {
    messageQueue.send(client, message);
  }

  /**
   * Associates given client connection with given unique client id.
   *
   * @param conn
   *          The client connection to attach to the given id
   * @param clientId
   *          The id to attach to the given client connection
   * @return The id that, in the end, is truly associated with the given client
   *         connection
   * @throws InputError
   *           If a client claims to have an id that is already in use
   */
  public String setClientId(Session conn, String clientId) throws InputError {
    Client client = clients.getBack(new Client(clientId));
    if (client == null) {
      clientId = conn.hashCode() + "";
      client = new Client(clientId);
      while (!clients.putNoOverwrite(conn, client)) {
        clientId = Math.random() + "";
        client = new Client(clientId);
      }
    } else if (!client.isConnected()) {
      clients.put(conn, client);
      client.toggleConnected();
    } else {
      throw new InputError("Don't steal identities");
    }
    return client.getId();
  }

  /**
   * Sends up-to-date list of lobbies to all clients not in lobbies.
   */
  public void updateLobbylessClients() {
    for (Client client : notInLobbies.values()) {
      sendLobbies(client);
    }
  }
}

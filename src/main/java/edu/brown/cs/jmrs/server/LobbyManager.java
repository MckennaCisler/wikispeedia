package edu.brown.cs.jmrs.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.server.errorhandling.ServerError;
import edu.brown.cs.jmrs.server.threading.ClosableReadWriteLock;

/**
 * Manager for lobbies of a server. Deals with instantiating, keeping track of,
 * and removing lobbies.
 *
 * @author shastin1
 *
 */
public class LobbyManager {

  private final ClosableReadWriteLock                 lock = new ClosableReadWriteLock();

  private ConcurrentHashMap<String, Lobby>            lobbies;
  private BiFunction<Server, String, ? extends Lobby> lobbyFactory;

  /**
   * Constructor specifying a 2-argument function which returns a lobby.
   *
   * @param lobbyFactory
   *          A "factory" for lobbies allowing instantiation when needed
   */
  public LobbyManager(
      BiFunction<Server, String, ? extends Lobby> lobbyFactory) {
    lobbies = new ConcurrentHashMap<>();
    this.lobbyFactory = lobbyFactory;
  }

  /**
   * Instantiates (and stores) and returns a new lobby with the given id, or
   * null if the id is already in use.
   *
   * @param lobbyId
   *          The id to assign to the new lobby
   * @param server
   *          The server instance to pass to the lobby
   * @return A new lobby with the given id, or null if said id is taken
   * @throws ServerError
   *           If the given id is already in use by another lobby
   */
  public Lobby create(
      String lobbyId,
      Server server,
      Client client,
      JsonObject args) throws ServerError {
    Lobby lobby = null;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      if (lobbies.containsKey(lobbyId)) {
        lobby = lobbies.get(lobbyId);
        if (lobby.isClosed()) {
          try (ClosableReadWriteLock temp2 = lock.lockWrite()) {
            lobbies.remove(lobbyId);
          }
        } else {
          return null;
        }
      }
      lobby = lobbyFactory.apply(server, lobbyId);
      try (ClosableReadWriteLock temp2 = lock.lockWrite()) {
        if (args != null) {
          lobby.init(args);
        }
        lobby.addClient(client.getId());
        client.setLobby(lobby);
        lobbies.put(lobbyId, lobby);
      }
    }
    return lobby;
  }

  /**
   * Returns the lobby associated with a given unique id, or null if no such
   * lobby exists.
   *
   * @param lobbyId
   *          The id to find a lobby associated with
   * @return The lobby associated with the given id
   */
  public Lobby get(String lobbyId) {
    Lobby lobby = null;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      lobby = lobbies.get(lobbyId);
      if (lobby != null && lobby.isClosed()) {
        try (ClosableReadWriteLock temp2 = lock.lockWrite()) {
          lobbies.remove(lobbyId);
        }
        lobby = null;
      }
    }
    return lobby;
  }

  /**
   * Returns a list of all currently existing lobbies.
   *
   * @return a list of all currently existing lobbies
   */
  public List<String> getOpenLobbies() {
    List<String> lobbyIds = new ArrayList<>();
    List<String> removeIds = new ArrayList<>();

    try (ClosableReadWriteLock temp = lock.lockRead()) {
      for (Entry<String, Lobby> lobby : lobbies.entrySet()) {
        if (lobby.getValue().isClosed()) {
          removeIds.add(lobby.getKey());
        } else {
          lobbyIds.add(lobby.getKey());
        }
      }
      try (ClosableReadWriteLock temp2 = lock.lockWrite()) {
        for (String id : removeIds) {
          lobbies.remove(id);
        }
      }
    }

    return lobbyIds;
  }

  /**
   * Removes a lobby given its unique id.
   *
   * @param lobbyId
   *          The id of the lobby to remove
   */
  public void remove(String lobbyId) {
    try (ClosableReadWriteLock temp = lock.lockWrite()) {
      lobbies.remove(lobbyId);
    }
  }
}

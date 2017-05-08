package edu.brown.cs.jmrs.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;

import edu.brown.cs.jmrs.server.customizable.Lobby;

/**
 * Manager for lobbies of a server. Deals with instantiating, keeping track of,
 * and removing lobbies.
 *
 * @author shastin1
 *
 */
public class LobbyManager {

  private final ReentrantReadWriteLock                rwl = new ReentrantReadWriteLock();
  private final Lock                                  r   = rwl.readLock();
  private final Lock                                  w   = rwl.writeLock();

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
   */
  public Lobby create(String lobbyId, Server server) {
    r.lock();
    if (lobbies.containsKey(lobbyId)) {
      Lobby lobby = lobbies.get(lobbyId);
      if (lobby.isClosed()) {
        r.unlock();
        w.lock();
        lobbies.remove(lobbyId);
        w.unlock();
        r.lock();
      } else {
        return null;
      }
    }
    r.unlock();
    Lobby lobby = lobbyFactory.apply(server, lobbyId);
    w.lock();
    lobbies.put(lobbyId, lobby);
    w.unlock();
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
    r.lock();
    Lobby lobby = lobbies.get(lobbyId);
    r.unlock();
    if (lobby != null && lobby.isClosed()) {
      w.lock();
      lobbies.remove(lobbyId);
      w.unlock();
      lobby = null;
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
    r.lock();
    for (Entry<String, Lobby> lobby : lobbies.entrySet()) {
      if (lobby.getValue().isClosed()) {
        removeIds.add(lobby.getKey());
      } else {
        lobbyIds.add(lobby.getKey());
      }
    }
    r.unlock();
    w.lock();
    for (String id : removeIds) {
      lobbies.remove(id);
    }
    w.unlock();

    return lobbyIds;
  }

  /**
   * Removes a lobby given its unique id.
   *
   * @param lobbyId
   *          The id of the lobby to remove
   */
  public void remove(String lobbyId) {
    try {
      w.lock();
      lobbies.remove(lobbyId);
    } finally {
      w.unlock();
    }
  }
}

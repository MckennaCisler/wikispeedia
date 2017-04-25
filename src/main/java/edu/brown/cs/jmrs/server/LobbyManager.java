package edu.brown.cs.jmrs.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;

import edu.brown.cs.jmrs.server.customizable.Lobby;

public class LobbyManager {

  private final ReentrantReadWriteLock                rwl = new ReentrantReadWriteLock();
  private final Lock                                  r   = rwl.readLock();
  private final Lock                                  w   = rwl.writeLock();

  private ConcurrentHashMap<String, Lobby>            lobbies;
  private BiFunction<Server, String, ? extends Lobby> lobbyFactory;

  public LobbyManager(
      BiFunction<Server, String, ? extends Lobby> lobbyFactory) {
    lobbies = new ConcurrentHashMap<>();
    this.lobbyFactory = lobbyFactory;
  }

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

  public void remove(String lobbyId) {
    w.lock();
    lobbies.remove(lobbyId);
    w.unlock();
  }

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
}

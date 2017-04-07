package edu.brown.cs.jmrs.networkexperiment;

import java.util.concurrent.ConcurrentHashMap;

public class LobbyManager extends ConcurrentHashMap<String, Lobby> {

  public synchronized Lobby getLobbyOf(Player player) {
    for (String lobbyName : keySet()) {
      Lobby lobby = get(lobbyName);
      if (lobby.contains(player)) {
        return lobby;
      }
    }

    return null;
  }

  public void removeFromLobby(Player player) {
    Lobby lobby = getLobbyOf(player);

    if (lobby != null) {
      lobby.remove(player);
    }
  }

  public synchronized void startLobby(Player player, String name) {
    put(name, new Lobby(player));
  }

  public synchronized void closeLobbyOf(Player player) {
    Lobby lobby = getLobbyOf(player);
    lobby.close();
  }

  public synchronized void closeLobby(Lobby lobby) {
    remove(lobby);
  }
}

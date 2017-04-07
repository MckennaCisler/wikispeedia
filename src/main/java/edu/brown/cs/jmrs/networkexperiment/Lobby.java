package edu.brown.cs.jmrs.networkexperiment;

import org.eclipse.jetty.util.ConcurrentHashSet;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class Lobby extends ConcurrentHashSet<Player> {

  public Lobby(Player host) {
    add(host);
  }

  public void close() {
    for (Player player : this) {
      player.messageClient("lobby closed");
    }

    ServerRunner.getInstance().getLobbies().closeLobby(this);
  }

  public void sendToPlayers(String message) {
    for (Player client : this) {
      client.messageClient(message);
    }
  }

  @Override
  public boolean add(Player e) {
    sendToPlayers(e.getName() + "has joined the lobby");

    return super.add(e);
  }

  @Override
  public boolean remove(Object o) {
    boolean removed = super.remove(o);
    sendToPlayers(((Player) o).getName() + "has left the lobby");

    if (isEmpty()) {
      close();
    }

    return removed;
  }
}
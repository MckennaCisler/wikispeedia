package edu.brown.cs.jmrs.server;

import java.util.Date;

import edu.brown.cs.jmrs.server.customizable.Lobby;

/**
 * Representation of a client with information on their lobby and connection
 * status to help direct commands.
 *
 * @author shastin1
 */
class Client implements Comparable<Client> {

  private String  id;
  private Lobby   lobby;
  private boolean connected;
  private boolean locked;
  private Date    cookieExpiration;

  /**
   * Constructor with id parameter, defaults connected to true, lobby to null,
   * and cookieExpration to null.
   *
   * @param id
   *          The id of the client
   */
  Client(String id) {
    this.id = id;
    connected = true;
  }

  @Override
  public int compareTo(Client p) {
    return cookieExpiration.compareTo(p.getCookieExpiration());
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Client) {
      Client po = (Client) o;

      if (po.getId().equals(getId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * returns the expiration date of the client's cookie.
   *
   * @return the expiration date of the client's cookie
   */
  public synchronized Date getCookieExpiration() {
    return cookieExpiration;
  }

  /**
   * Returns the client's unique id.
   *
   * @return the client's unique id
   */
  public String getId() {
    return id;
  }

  public synchronized boolean locked() {
    return locked;
  }

  public synchronized void lock(boolean discVal) {
    locked = discVal;
  }

  /**
   * returns the client's lobby.
   *
   * @return the client's lobby
   */
  public synchronized Lobby getLobby() {
    return lobby;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  /**
   * Returns whether the client is connected.
   *
   * @return whether the client is connected
   */
  public synchronized boolean isConnected() {
    return connected;
  }

  /**
   * Sets the expiration date of the cookie, so the client's information will be
   * erased from the server if they disconnect.
   *
   * @param date
   *          The expiration date of the client's cookie
   */
  public synchronized void setCookieExpiration(Date date) {
    this.cookieExpiration = date;
  }

  /**
   * Sets the lobby the client is in.
   *
   * @param lobby
   *          The lobby to direct client commands to
   */
  public synchronized void setLobby(Lobby lobby) {
    this.lobby = lobby;
  }

  /**
   * Toggles the status of connected (true -> false or vice versa) and returns
   * the old value.
   *
   * @return the old value of connected
   */
  public synchronized boolean toggleConnected() {
    if (connected) {
      connected = false;
      return true;
    } else {
      connected = true;
      return false;
    }
  }

  @Override
  public String toString() {
    return "Player [id=" + id + ", lobby=" + lobby + ", connected=" + connected
        + "]";
  }
}

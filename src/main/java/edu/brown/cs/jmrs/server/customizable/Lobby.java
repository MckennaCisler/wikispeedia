package edu.brown.cs.jmrs.server.customizable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.InputError;

/**
 * Lobby interface for creating different types of lobbies to run on the server
 * framework.
 *
 * @author shastin1
 *
 */
public interface Lobby {

  /**
   * Adds client to this lobby.
   *
   * @param clientId
   *          The id of the client who joined the lobby
   */
  void addClient(String clientId);

  /**
   * Initializes the lobby with parameters in a JsonObject.
   *
   * @param jsonElement
   *          Json of the parameters to initialize the lobby with
   * @throws InputError
   *           If the lobby fails to initialize
   */
  void init(JsonObject jsonElement) throws InputError;

  /**
   * Returns whether the lobby has been closed.
   *
   * @return whether the lobby has been closed
   */
  boolean isClosed();

  /**
   * Called when a player disconnects while in the lobby.
   *
   * @param clientId
   *          the id of the client disconnecting
   */
  void playerDisconnected(String clientId);

  /**
   * Called when a player reconnects after disconnecting.
   *
   * @param clientId
   *          The id of the client reconnecting
   */
  void playerReconnected(String clientId);

  /**
   * Removes client from this lobby.
   *
   * @param clientId
   *          The id of the client who left the lobby
   */
  void removeClient(String clientId);

  /**
   * Converts lobby to json for sending to client.
   *
   * @param gson
   *          Gson instance to be used for serialization if that is implemented
   *          by the lobby
   * @return The lobby as a JsonElement
   */
  JsonElement toJson(Gson gson);
}

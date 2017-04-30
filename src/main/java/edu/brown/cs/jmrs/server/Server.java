package edu.brown.cs.jmrs.server;

import java.io.IOException;
import java.util.function.BiFunction;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.server.threading.GlobalThreadManager;

/**
 * User access point to server functionality, keeps functionality black-boxed.
 *
 * @author shastin1
 *
 */
@WebSocket
public class Server {

  protected ServerWorker       server;
  protected CommandInterpreter interpreter;
  protected final Gson         gson;

  public Server(
      BiFunction<Server, String, ? extends Lobby> lobbyFactory,
      CommandInterpreter interpreter,
      Gson gson) {
    this.interpreter = interpreter;
    server = new ServerWorker(this, lobbyFactory, gson);
    this.gson = gson;
  }

  public void sendToClient(String playerId, String message) {
    try {
      server.getClient(playerId).getRemote().sendString(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void closeLobby(String lobbyId) {
    server.closeLobby(lobbyId);
  }

  @OnWebSocketClose
  public void onClose(Session conn, int code, String reason) {
    GlobalThreadManager.submit(new ClientDisconnectedHandler(server, conn));
  }

  @OnWebSocketMessage
  public void onMessage(Session conn, String message) {
    GlobalThreadManager.submit(
        new ServerCommandHandler(server, conn, message, interpreter, gson));
  }

  @OnWebSocketConnect
  public void onOpen(Session conn) throws Exception {
    GlobalThreadManager.submit(new ClientConnectedHandler(server, conn));
  }
}

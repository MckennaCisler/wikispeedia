package edu.brown.cs.jmrs.networkexperiment;

import java.net.InetSocketAddress;
import java.nio.channels.NotYetConnectedException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class ServerRunner extends WebSocketServer {

  private Queue<Integer>  availablePorts;
  private ExecutorService daemonManager;

  private int             currentPort;

  public ServerRunner(int port) {
    super(new InetSocketAddress(port));
    int maxDaemons = Runtime.getRuntime().availableProcessors() - 1;
    daemonManager = Executors.newFixedThreadPool(maxDaemons);

    availablePorts = new ConcurrentLinkedQueue<>();
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    System.out.println(
        "Currently " + connections().size() + " people not in lobbies.");
  }

  @Override
  public void onError(WebSocket conn, Exception ex) {
    ex.printStackTrace();
  }

  public Future<?> submit(Runnable task) {
    return daemonManager.submit(task);
  }

  public synchronized int generatePort() {
    if (availablePorts.isEmpty()) {
      return ++currentPort;
    } else {
      return availablePorts.poll();
    }
  }

  public synchronized void freePort(int port) {
    boolean notCurrentPort = false;

    while (currentPort > port) {
      if (availablePorts.contains(currentPort)) {
        currentPort--;
      } else {
        notCurrentPort = true;
        break;
      }
    }

    if (notCurrentPort) {
      availablePorts.add(port);
    }
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    Future<?> operationResult = daemonManager
        .submit(new LobbyCommand(this, message));
    try {
      String retMessage = (String) operationResult.get();
      conn.send(retMessage);
    } catch (
        NotYetConnectedException
        | InterruptedException
        | ExecutionException e) {
      e.printStackTrace();
    }
    System.out.println(
        "currently less than " + currentPort + 1 + " lobbies running.");
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    System.out.println(
        "Currently " + connections().size() + " people not in lobbies.");
  }
}

package edu.brown.cs.jmrs.networkexperiment;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PortGenerator extends ConcurrentLinkedQueue<Integer> {

  public PortGenerator(int port) {
    super();
    currentPort = port;
  }

  private int currentPort;

  public int generatePort() {
    if (isEmpty()) {
      return ++currentPort;
    } else {
      return poll();
    }
  }

  public void freePort(int port) {
    boolean notCurrentPort = false;

    while (currentPort > port) {
      if (contains(currentPort)) {
        currentPort--;
      } else {
        notCurrentPort = true;
        break;
      }
    }

    if (notCurrentPort) {
      add(port);
    }
  }
}

package edu.brown.cs.jmrs.main;

import java.io.IOException;

import com.google.common.collect.ImmutableList;

import edu.brown.cs.jmrs.networkexperiment.ServerRunner;
import edu.brown.cs.jmrs.web.WikiHandlers;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Main {

  public static final int DEFAULT_SPARK_PORT = 4567;
  public static final int DEFAULT_SOCKET_PORT = 4568;

  public static void main(String[] args) {
    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("spark-port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_SPARK_PORT);
    parser.accepts("socket-port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_SOCKET_PORT);
    OptionSet options = parser.parse(args);

    if (options.has("gui")) {
      ServerRunner runner =
          new ServerRunner((int) options.valueOf("socket-port"));
      try {
        runner.start();

        SparkServer.runSparkServer((int) options.valueOf("spark-port"),
            ImmutableList.of(new WikiHandlers()));
      } finally {
        try {
          runner.stop();
        } catch (IOException | InterruptedException e) {
          System.out.println("ERROR: " + e.getMessage());
        }
        SparkServer.stop();
      }
    }

    /*
     * Now open src/main/resources/network-experiment-html/html.html in a
     * browser and open the javascript console. All users are anonymous by
     * default, but you can use the command setName(String newname) to set a
     * username. Send a message to the chat with message(String message)
     */
  }
}

package edu.brown.cs.jmrs.main;

import edu.brown.cs.jmrs.networkexperiment.ServerRunner;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Main {

  public static final int DEFAULT_PORT = 4567;

  public static void main(String[] args) {
    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);
    OptionSet options = parser.parse(args);

    ServerRunner runner = new ServerRunner((int) options.valueOf("port"));

    /*
     * Now open src/main/resources/network-experiment-html/html.html in a
     * browser and open the javascript console. All users are anonymous by
     * default, but you can use the command setName(String newname) to set a
     * username. Send a message to the chat with message(String message)
     */
  }

}

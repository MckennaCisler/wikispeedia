package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * Interprets commands over a Wiki lobby.
 *
 * @author mcisler
 *
 */
public class WikiInterpreter implements CommandInterpreter {
  public static final Gson GSON = registerSerializers(); // TODO

  /**
   * Registers custom Json (Gson) serializers for this project.
   *
   * https://github.com/google/gson/blob/master/
   * UserGuide.md#TOC-Custom-Serialization-and-Deserialization
   *
   * @return A Gson Object with the register Serializers.
   */
  private static Gson registerSerializers() {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(WikiPage.class, new WikiPage.Serializer());

    return builder.create();
  }

  /**
   * All possible incoming and outgoing commands for the WikiInterpreter. Also
   * used in WikiLobby for OUTGOING commands.
   *
   * @author mcisler
   *
   */
  enum Command {
    /**
     * INCOMING Commands.
     */
    // Lobby-specific commands
    GET_PLAYERS("get_players", COMMAND_TYPE.INCOMING, "lobby_id"), //
    GET_TIME("get_time", COMMAND_TYPE.INCOMING, "lobby_id"), //
    GET_SETTINGS("get_settings", COMMAND_TYPE.INCOMING, "lobby_id"), //
    // Player-specific commands
    GOTO_PAGE("goto_page", COMMAND_TYPE.INCOMING, "player_id", "page_name"), //
    GET_PATH("get_path", COMMAND_TYPE.INCOMING, "player_id"), //
    // GET_LINKS("get_links", COMMAND_TYPE.INCOMING, "player_id");
    /**
     * RESPONSEs to INCOMING Commands.
     */
    // Lobby-specific commands
    RETURN_PLAYERS("return_players", COMMAND_TYPE.RESPONSE, "lobby_id"), //
    RETURN_TIME("return_time", COMMAND_TYPE.RESPONSE, "lobby_id"), //
    RETURN_SETTINGS("return_settings", COMMAND_TYPE.RESPONSE, "lobby_id"), //
    RETURN_PAGE("goto_page", COMMAND_TYPE.RESPONSE, "player_id", "page_name"), //
    RETURN_PATH("return_path", COMMAND_TYPE.RESPONSE, "player_id"), //
    ERROR("error", COMMAND_TYPE.RESPONSE, "player_id"), //

    /**
     * OUTGOING Server Commands.
     */
    END_GAME("end_game", COMMAND_TYPE.OUTGOING, "lobby_id", "winner_id");

    private static enum COMMAND_TYPE {
      INCOMING, // incoming commands for RESPONSEs
      RESPONSE, // responses to INCOMING commands
      OUTGOING; // server event-based commands
    }

    private final String command;
    private final COMMAND_TYPE type;
    private String[] args;

    /**
     * Creates a particular defined command based on command.
     *
     * @param command
     *          The command to create.
     */
    Command(String command, COMMAND_TYPE type, String... args) {
      this.command = command;
      this.type = type;
      this.args = args;
    }

    public String command() {
      return command;
    }

    public String[] args() {
      return args;
    }

    /**
     * Builds the given command from the provided arguments.
     *
     * @param data
     *          An object to use GSON to convert into JSON.
     * @return A JSON string ready for sending.
     */
    public String build(Object data) {
      assert type.equals(COMMAND_TYPE.RESPONSE)
          || type.equals(COMMAND_TYPE.OUTGOING);
      JsonObject root = new JsonObject();
      root.addProperty("type", command);
      root.addProperty("payload", GSON.toJson(data));
      return GSON.toJson(root);
    }
  }

  @Override
  public void interpret(Lobby uncastLobby, String clientId,
      Map<String, ?> command) {
    WikiLobby lobby = (WikiLobby) uncastLobby;
    String cname = (String) command.get("command");

    if (cname.equals(Command.GET_PLAYERS.command())) {
      lobby.getServer().sendToClient(clientId,
          Command.RETURN_PLAYERS.build(lobby.getPlayers()));

    } else if (cname.equals(Command.GET_TIME.command())) {
      lobby.getServer().sendToClient(clientId,
          Command.RETURN_TIME.build(lobby.getPlayTime()));

    } else if (cname.equals(Command.GET_SETTINGS.command())) {
      String result = new String();
      result =
          Command.RETURN_SETTINGS
              .build(ImmutableMap.builder().put("start", lobby.getStartPage())
                  .put("goal", lobby.getGoalPage())
                  .put("start_time", lobby.getStartTime()).build()); // TODO
      lobby.getServer().sendToClient(clientId, result);

    } else if (cname.equals(Command.GOTO_PAGE.command())) {
      String result = new String();
      WikiPlayer player = lobby.getPlayer(clientId);
      WikiPage curPlayerPage = player.getCurPage();

      String reqPage = (String) command.get("page_name");
      String curPage = player.getCurPage().getName();
      try {
        if (player.goToPage(new WikiPage(reqPage))) {
          // if could go to page (and thus did go to page)
          result =
              Command.RETURN_PAGE.build(ImmutableMap.of("text",
                  player.getCurPage().getInnerContent(lobby.getLinkFinder()),
                  "links", player.getLinks()));
        } else {
          // if we can't go to the page, revert to the previous current
          result =
              Command.RETURN_PAGE.build(ImmutableMap.of("error",
                  String.format("Player cannot move from page %s to %s",
                      curPage, reqPage),
                  "text", curPlayerPage.getInnerContent(lobby.getLinkFinder()),
                  "links", lobby.getLinkFinder().linkedPages(curPlayerPage)));
        }
      } catch (IOException e1) {
        try {
          result =
              Command.RETURN_PAGE.build(ImmutableMap.of("error", String.format(
                  "Error in accessing page %s: %s", curPage, e1.getMessage(),
                  "text", curPlayerPage.getInnerContent(lobby.getLinkFinder()),
                  "links", lobby.getLinkFinder().linkedPages(curPlayerPage))));
        } catch (IOException e2) {
          // this should never happen (this page shoudl've been cached and
          // already visited. TODO: COuld it?
        }
      }
      lobby.getServer().sendToClient(clientId, result);
      lobby.update(); // check for winner

    } else if (cname.equals(Command.GET_PATH.command())) {
      String result = new String();
      WikiPlayer player = lobby.getPlayer(clientId);
      result = Command.RETURN_PATH.build(player.getPath());
      lobby.getServer().sendToClient(clientId, result);

    } else {
      lobby.getServer().sendToClient(clientId, Command.ERROR.build(
          ImmutableMap.of("error", "Invalid command specified: " + cname)));
    }
  }

}

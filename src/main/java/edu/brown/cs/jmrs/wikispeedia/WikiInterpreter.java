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
    RETURN_PAGE("return_page", COMMAND_TYPE.RESPONSE, "player_id", "page_name"), //
    RETURN_PATH("return_path", COMMAND_TYPE.RESPONSE, "player_id"), //
    ERROR("error", COMMAND_TYPE.RESPONSE, "player_id"), //

    /**
     * OUTGOING Server Commands.
     */
    END_GAME("end_game", COMMAND_TYPE.OUTGOING, "lobby_id", "winner_id"),

    // Value to signify not in enum (when using switch statement)
    NULL(null, null);

    /**
     * The type of commands in this enum.
     */
    enum COMMAND_TYPE {
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
      return build(data, "");
    }

    /**
     * Builds the given command from the provided arguments.
     *
     * @param data
     *          An object to use GSON to convert into JSON.
     * @param errorMessage
     *          An optional error message to display.
     * @return A JSON string ready for sending.
     */
    public String build(Object data, String errorMessage) {
      assert type.equals(COMMAND_TYPE.RESPONSE)
          || type.equals(COMMAND_TYPE.OUTGOING);
      JsonObject root = new JsonObject();
      root.addProperty("command", command);
      root.addProperty("error_message", errorMessage);
      root.add("payload", GSON.toJsonTree(data));
      return GSON.toJson(root);
    }

    /**
     * Equivalent to valueOf() but returns a blank string instead of an
     * exception.
     */
    static Command safeValueOf(String commandName) {
      try {
        return valueOf(commandName);
      } catch (IllegalArgumentException | NullPointerException e) {
        return NULL;
      }
    }
  }

  @Override
  public void interpret(Lobby uncastLobby, String clientId,
      JsonObject command) {
    WikiLobby lobby = (WikiLobby) uncastLobby;
    String cname = command.get("command").getAsString();

    // switch > if/else
    switch (Command.safeValueOf(cname.toUpperCase())) {
      case GET_PLAYERS:
        lobby.getServer().sendToClient(clientId,
            Command.RETURN_PLAYERS.build(lobby.getPlayers()));
        break;
      case GET_TIME:
        lobby.getServer().sendToClient(clientId,
            Command.RETURN_TIME.build(lobby.getPlayTime()));
        break;
      case GET_SETTINGS:
        String json0 = new String();
        json0 =
            Command.RETURN_SETTINGS
                .build(ImmutableMap.builder().put("start", lobby.getStartPage())
                    .put("goal", lobby.getGoalPage())
                    .put("start_time", lobby.getStartTime()).build()); // TODO
        lobby.getServer().sendToClient(clientId, json0);
        break;
      case GOTO_PAGE:
        String json1 = new String();
        WikiPlayer player = lobby.getPlayer(clientId);
        WikiPage curPlayerPage = player.getCurPage();

        assert command.get("payload").isJsonObject();
        String reqPage =
            command.get("payload").getAsJsonObject().get("page_name")
                .getAsString();
        String curPage = player.getCurPage().getName();
        try {
          WikiPage reqWikiPage = WikiPage.fromAny(reqPage);
          if (player.goToPage(reqWikiPage)) {
            // if could go to page (and thus did go to page)
            json1 =
                Command.RETURN_PAGE
                    .build(getPlayerPageInfo(reqWikiPage, lobby));
          } else {
            // if we can't go to the page, revert to the previous current
            json1 =
                Command.RETURN_PAGE.build(
                    getPlayerPageInfo(curPlayerPage, lobby),
                    String.format("Player cannot move from page %s to %s",
                        curPage, reqPage));
          }
        } catch (IOException e1) {
          try {
            json1 =
                Command.RETURN_PAGE.build(
                    getPlayerPageInfo(curPlayerPage, lobby),
                    String.format("Error in accessing page %s: %s", curPage,
                        e1.getMessage()));
          } catch (IOException e2) {
            // this should never happen (this page should've been cached and
            // already visited. TODO: Could it?
          }
        }
        lobby.getServer().sendToClient(clientId, json1);
        lobby.update(); // check for winner
        break;
      case GET_PATH:
        WikiPlayer player2 = lobby.getPlayer(clientId);
        String json2 = Command.RETURN_PATH.build(player2.getPath());
        lobby.getServer().sendToClient(clientId, json2);
        break;
      default:
        lobby.getServer().sendToClient(clientId, Command.ERROR
            .build(ImmutableMap.of(), "Invalid command specified: " + cname));
    }
  }

  private Map<String, ?> getPlayerPageInfo(WikiPage page, WikiLobby lobby)
      throws IOException {
    return ImmutableMap.of("href", page.url(), "title", page.getTitle(), "text",
        lobby.getContentFormatter()
            .stringFormat(page.linksMatching(lobby.getLinkFinder())),
        "links", lobby.getLinkFinder().linkedPages(page));
  }
}

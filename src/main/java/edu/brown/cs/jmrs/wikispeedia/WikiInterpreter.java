package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * Interprets commands over a Wiki lobby.
 *
 * @author mcisler
 *
 */
public class WikiInterpreter implements CommandInterpreter {
  private static final Gson GSON = registerSerializers();

  private final WikiLobby lobby;
  private final String playerId;

  /**
   * @param playerId
   *          ??
   * @param lobby
   *          The lobby this command was called to.
   */
  public WikiInterpreter(String playerId, WikiLobby lobby) {
    this.playerId = playerId;
    this.lobby = lobby;
  }

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
    // builder.registerTypeAdapter(WikiPage.class, new WikiPage.Serializer());

    return builder.create();
  }

  /**
   * All possible commands for the WikiInterpreter.
   *
   * @author mcisler
   *
   */
  private enum Command {
    // Lobby-specific commands
    GET_PLAYERS("get_players", "lobby_id"), //
    GET_WINNER("get_winner", "lobby_id"), //
    GET_TIME("get_time", "lobby_id"), //
    GET_SETTINGS("get_settings", "lobby_id"), //
    // Player-specific commands
    GOTO_PAGE("goto_page", "player_id", "page_name"), //
    GET_PATH("get_path", "player_id"); //
    // GET_LINKS("get_links", "player_id");

    private final String command;
    private String[] args;

    /**
     * Creates a particular defined command based on command.
     *
     * @param command
     *          The command to create.
     */
    Command(String command, String... args) {
      this.command = command;
      this.args = args;
    }

    public String command() {
      return command;
    }

    public String[] args() {
      return args;
    }
  }

  @Override
  public void interpret(Map<String, ?> command) {
    String cname = (String) command.get("command");
    if (cname.equals(Command.GET_PLAYERS.command())) {
      String result = new String();
      result = GSON.toJson(lobby.getPlayers());

    } else if (cname.equals(Command.GET_WINNER.command())) {
      String result = new String();
      result = GSON.toJson(lobby.getWinner());

    } else if (cname.equals(Command.GET_TIME.command())) {
      String result = new String();
      result = GSON.toJson(lobby.getPlayTime());

    } else if (cname.equals(Command.GET_SETTINGS.command())) {
      String result = new String();
      result =
          GSON.toJson(ImmutableMap.builder().put("start", lobby.getStartPage())
              .put("goal", lobby.getGoalPage())
              .put("start_time", lobby.getStartTime()).build()); // TODO

    } else if (cname.equals(Command.GOTO_PAGE.command())) {
      String result = new String();
      WikiPlayer player = lobby.getPlayer((String) command.get("player_id"));

      // if could go to page (and thus did go to page)
      String reqPage = (String) command.get("page_name");
      String curPage = player.getCurPage().getName();
      try {
        if (player.goToPage(new WikiPage(reqPage))) {
          result =
              GSON.toJson(
                  ImmutableMap.of("text", player.getCurPage().getInnerContent(),
                      "links", player.getLinks()));
        } else {
          result =
              GSON.toJson(ImmutableMap.of("error",
                  String.format("Player cannot move from page %s to %s",
                      curPage, reqPage),
                  "text", player.getCurPage().getInnerContent(), "links",
                  player.getLinks()));
        }
      } catch (IOException e) {
        result =
            GSON.toJson(ImmutableMap.of("error", String.format(
                "Error in accessing page %s: %s", curPage, e.getMessage())));
      }

    } else if (cname.equals(Command.GET_PATH.command())) {
      String result = new String();
      WikiPlayer player = lobby.getPlayer((String) command.get("player_id"));
      result = GSON.toJson(player.getPath());

    } else {
      String result = new String();
      result =
          GSON.toJson(
              ImmutableMap.of("error", "Invalid command specified: " + cname));
    }
  }

}

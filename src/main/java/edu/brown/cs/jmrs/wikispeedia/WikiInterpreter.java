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
  static final Gson GSON = registerSerializers(); // TODO

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

  @Override
  public void interpret(Lobby uncastLobby, String clientId,
      JsonObject command) {
    WikiLobby lobby = (WikiLobby) uncastLobby;
    String cname = command.get("command").getAsString();
    // note that Server will have asserted that command has a command & payload

    // switch > if/else
    switch (Command.safeValueOf(cname.toUpperCase())) {
      case GET_PLAYERS:
        Command.sendAllPlayers(lobby);
        break;

      case GET_TIME:
        Command.RETURN_TIME.send(lobby.getServer(), clientId,
            lobby.getPlayTime());
        break;

      case GET_SETTINGS:
        Map<Object, Object> settings =
            ImmutableMap.builder().put("start", lobby.getStartPage())
                .put("goal", lobby.getGoalPage())
                .put("start_time", lobby.getStartTime()).build();

        Command.RETURN_SETTINGS.send(lobby.getServer(), clientId, settings);
        break;

      case FORCE_BEGIN_GAME:
        try {
          lobby.start();
          Command.sendBeginGame(lobby);
        } catch (IllegalStateException e) {
          Command.sendBeginGame(lobby, e.getMessage());
        }
        break;

      case SET_USERNAME:
        lobby.setPlayerName(clientId, command.get("payload").getAsJsonObject()
            .get("username").getAsString());
        Command.RETURN_SET_USERNAME.send(lobby.getServer(), clientId,
            ImmutableMap.of());
        break;

      case SET_PLAYER_STATE:
        setPlayerStateHandler(lobby, clientId, command);
        break;

      case GET_PAGE:
        getPageHandler(lobby, clientId, command);
        break;

      case GOTO_PAGE:
        gotoPageHandler(lobby, clientId, command);
        break;

      case GET_PATH:
        WikiPlayer player = lobby.getPlayer(clientId);
        Command.RETURN_PATH.send(lobby.getServer(), clientId, player.getPath());
        break;

      default:
        Command.ERROR.send(lobby.getServer(), clientId, ImmutableMap.of(),
            "Invalid command specified: " + cname);
    }
  }

  private void setPlayerStateHandler(WikiLobby lobby, String clientId,
      JsonObject command) {
    boolean state =
        command.get("payload").getAsJsonObject().get("state").getAsBoolean();
    lobby.getPlayer(clientId).setReady(state);

    // update other players and possibly start game
    lobby.checkAllReady();
  }

  private void getPageHandler(WikiLobby lobby, String clientId,
      JsonObject command) {
    String identifier =
        command.get("payload").getAsJsonObject().get("page_name").getAsString();
    try {
      Command.RETURN_GET_PAGE.send(lobby.getServer(), clientId,
          getPlayerPageInfo(WikiPage.fromAny(identifier), lobby));
    } catch (IOException e) {
      Command.RETURN_GET_PAGE.send(lobby.getServer(), clientId,
          ImmutableMap.of(), String.format("Error in accessing page %s: %s",
              identifier, e.getMessage()));
    }
  }

  private void gotoPageHandler(WikiLobby lobby, String clientId,
      JsonObject command) {
    WikiPlayer player = lobby.getPlayer(clientId);
    WikiPage curPlayerPage = player.getCurPage();
    Map<String, ?> curPageInfo = ImmutableMap.of();

    assert command.get("payload").isJsonObject();
    String reqPage =
        command.get("payload").getAsJsonObject().get("page_name").getAsString();
    String curPage = player.getCurPage().getName();
    try {
      WikiPage reqWikiPage = WikiPage.fromAny(reqPage);
      if (player.goToPage(reqWikiPage)) {
        // if could go to page (and thus did go to page)
        Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId,
            getPlayerPageInfo(reqWikiPage, lobby));

        // on success, check for winner, in which case we'll send an additional
        // mass message
        if (!lobby.checkForWinner()) {
          // if there wasn't a winner, update all player's on eachother's status
          // (i.e. when one moves)
          Command.sendAllPlayers(lobby);
        }

      } else {
        // if we can't go to the page, revert to the previous current
        Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId,
            getPlayerPageInfo(curPlayerPage, lobby), String.format(
                "Player cannot move from page %s to %s", curPage, reqPage));
      }
      curPageInfo = getPlayerPageInfo(curPlayerPage, lobby);
    } catch (IOException e) {
      Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId, curPageInfo,
          String.format("Error in accessing page %s: %s", curPage,
              e.getMessage()));

    } catch (IllegalStateException es) {
      Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId, curPageInfo,
          es.getMessage());
    }
  }

  private Map<String, ?> getPlayerPageInfo(WikiPage page, WikiLobby lobby)
      throws IOException {
    return ImmutableMap.of("href", page.url(), "title", page.getTitle(),
        "blurb", page.getBlurb(), "text",
        lobby.getContentFormatter()
            .stringFormat(page.linksMatching(lobby.getLinkFinder())),
        "links", lobby.getLinkFinder().linkedPages(page));
  }
}

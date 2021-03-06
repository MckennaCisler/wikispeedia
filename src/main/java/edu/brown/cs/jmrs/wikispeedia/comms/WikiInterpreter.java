package edu.brown.cs.jmrs.wikispeedia.comms;

import java.io.IOException;
import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.wikispeedia.WikiLobby;
import edu.brown.cs.jmrs.wikispeedia.WikiPlayer;

/**
 * Interprets commands over a Wiki lobby.
 *
 * @author mcisler
 *
 */
public class WikiInterpreter implements CommandInterpreter {
  @Override
  public void interpret(Lobby uncastLobby, String clientId,
      JsonObject command) {
    assert uncastLobby instanceof WikiLobby;
    WikiLobby lobby = (WikiLobby) uncastLobby;
    String cname = command.get("command").getAsString();
    JsonObject commandPayload = command.get("payload").getAsJsonObject();
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
        Command.RETURN_SETTINGS.send(lobby.getServer(), clientId, lobby);
        break;

      case FORCE_BEGIN_GAME:
        try {
          lobby.start(true);
          Command.sendBeginGame(lobby);
        } catch (IllegalStateException e) {
          Main.debugLog(String
              .format("Lobby %s failed to start in force begin game", lobby));
          Command.sendBeginGame(lobby, e.getMessage());
        }
        break;

      case SET_USERNAME:
        lobby.setPlayerName(clientId,
            commandPayload.get("username").getAsString());
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

      case GO_BACK_PAGE:
        goBackPageHandler(lobby, clientId, command);
        break;

      case GET_PATH:
        WikiPlayer player = lobby.getPlayer(clientId);
        Command.RETURN_PATH.send(lobby.getServer(), clientId, player.getPath());
        break;

      case SEND_MESSAGE:
        lobby.registerMessage(command.get("payload").getAsJsonObject()
            .get("message").getAsString(), clientId);
        for (WikiPlayer client : lobby.getConnectedPlayers()) {
          lobby.sendMessagesToPlayer(client.getId());
        }
        break;

      case GET_MESSAGES:
        lobby.sendMessagesToPlayer(clientId);
        break;

      default:
        Command.sendError(lobby.getServer(), clientId,
            "Invalid command specified: " + cname);
    }
  }

  private void setPlayerStateHandler(WikiLobby lobby, String clientId,
      JsonObject command) {
    boolean state =
        command.get("payload").getAsJsonObject().get("state").getAsBoolean();
    lobby.getPlayer(clientId).setReady(state);
  }

  private void getPageHandler(WikiLobby lobby, String clientId,
      JsonObject command) {
    String identifier =
        command.get("payload").getAsJsonObject().get("page_name").getAsString();
    try {
      Command.RETURN_GET_PAGE.send(lobby.getServer(), clientId,
          getPlayerPageInfo(
              WikiPage.fromAny(identifier, Main.WIKI_PAGE_DOC_CACHE), lobby));
    } catch (IOException e) {
      e.printStackTrace();
      Command.RETURN_GET_PAGE.send(lobby.getServer(), clientId,
          ImmutableMap.of(), String.format("Could not access page %s: %s",
              identifier, e.getMessage()));
    }
  }

  private void gotoPageHandler(WikiLobby lobby, String clientId,
      JsonObject command) {
    WikiPlayer player = lobby.getPlayer(clientId);
    WikiPage curPlayerPage = player.getCurPage();
    JsonObject curPageInfo = getCurPlayerPageInfo(lobby, player);

    assert command.get("payload").isJsonObject();

    try {
      // if the player is just requesting their initial page, give it to them
      if (command.get("payload").getAsJsonObject().has("initial") && command
          .get("payload").getAsJsonObject().get("initial").getAsBoolean()) {

        Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId,
            getPlayerPageInfo(curPlayerPage, lobby));
      } else {
        String reqPage =
            command.get("payload").getAsJsonObject().get("page_name")
                .getAsString();

        Main.debugLog(player.getName() + " going to " + reqPage);

        WikiPage reqWikiPage =
            WikiPage.fromAny(reqPage, Main.WIKI_PAGE_DOC_CACHE);
        if (player.goToPage(reqWikiPage)) {
          // if could go to page (and thus did go to page)
          Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId,
              getPlayerPageInfo(reqWikiPage, lobby));

        } else {
          // if we can't go to the page, revert to the previous current
          Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId,
              curPageInfo, String.format("Cannot move from page %s to %s",
                  curPlayerPage.getName(), reqPage));
        }

      }
    } catch (IOException e) {
      Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId, curPageInfo,
          String.format("Could not access page %s: %s", curPlayerPage.getName(),
              e.getMessage()));

    } catch (IllegalStateException es) {
      Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId, curPageInfo,
          es.getMessage());
    }
  }

  private void goBackPageHandler(WikiLobby lobby, String clientId,
      JsonObject command) {
    WikiPlayer player = lobby.getPlayer(clientId);
    JsonObject curPageInfo = getCurPlayerPageInfo(lobby, player);
    assert command.get("payload").isJsonObject();

    try {

      String reqPage;
      if (command.get("payload").getAsJsonObject().has("page_name")) {
        reqPage =
            command.get("payload").getAsJsonObject().get("page_name")
                .getAsString();
      } else {
        reqPage =
            player.getPath().get(player.getPath().size() - 1).getPage().url();
      }

      Main.debugLog(String.format("%s going back to %s; history: %s",
          player.getName(), reqPage, player.getPath()));

      WikiPage reqPrevPage =
          WikiPage.fromAny(reqPage, Main.WIKI_PAGE_DOC_CACHE);

      // try to go back; it'll throw a NoSuchElementException on failure
      // and will set player.getCurPage() to the right value
      player.goBackPage(reqPrevPage);

      Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId,
          getPlayerPageInfo(player.getCurPage(), lobby));

    } catch (NoSuchElementException e) {
      Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId, curPageInfo,
          e.getMessage());

    } catch (IOException ioe) {
      Command.RETURN_GOTO_PAGE.send(lobby.getServer(), clientId, curPageInfo,
          "Cannot go back to previous page: " + ioe.getMessage());
    }
  }

  private static JsonObject getCurPlayerPageInfo(WikiLobby lobby,
      WikiPlayer player) {
    WikiPage curPlayerPage = player.getCurPage();
    try {
      return getPlayerPageInfo(curPlayerPage, lobby);
    } catch (IOException e1) {
      // we're just gonna have to send null if this happens
      throw new AssertionError("Current player page could not be retrieved",
          e1);
    }
  }

  /**
   * Gets a more informative version of a page.
   *
   * @param page
   *          The page to get
   * @param lobby
   *          THe lobby to get required things from.
   * @return The serialized page.
   * @throws IOException
   *           If it can't be accessed.
   */
  public static JsonObject getPlayerPageInfo(WikiPage page, WikiLobby lobby)
      throws IOException {
    JsonObject info = new JsonObject();
    info.addProperty("href", page.url());
    info.addProperty("name", page.getName());
    info.addProperty("title", page.getTitle());
    info.addProperty("blurb",
        page.getFormattedBlurb(lobby.getContentFormatter()));
    info.addProperty("text", lobby.getContentFormatter()
        .stringFormat(page.linksMatching(lobby.getLinkFinder())));
    info.add("links",
        Main.GSON.toJsonTree(lobby.getLinkFinder().linkedPages(page)));
    return info;
  }
}

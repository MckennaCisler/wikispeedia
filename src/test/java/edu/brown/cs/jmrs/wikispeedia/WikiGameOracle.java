package edu.brown.cs.jmrs.wikispeedia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.InputError;
import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * An oracle tester to randomly generate games and runs them through WikiLobby.
 *
 * NOTE: The probabilities in this are finely tuned to make sure games finish in
 * a reasonable time. There are many that are VERY hard to tune!
 *
 * @author mcisler
 */
public class WikiGameOracle {
  private static final int NUM_LOBBIES_TO_TEST = 1;

  private static final int NUM_TIME_TRIAL_GEN_PLAYERS = 16;
  private static final int TIME_TRIAL_GAME_PAGE_DIST  = 1;

  /**
   * The oracle for time trial oracle.
   */
  @Test
  public void timeTrialOracle() {
    // don't run normally
    if (Main.DEBUG) {
      for (int i = 0; i < NUM_LOBBIES_TO_TEST; i++) {
        WikiGame game = getGame(TIME_TRIAL_GAME_PAGE_DIST);
        WikiLobby lobby =
            getLobby(WikiGameMode.Mode.TIME_TRIAL, NUM_TIME_TRIAL_GEN_PLAYERS,
                game);
        setupPlayers(lobby);
        // don't have to start because all ready
        assertTrue(lobby.started());
        playGame(lobby, game, this::checkTimeTrialEndState);
      }
    }
  }

  /**
   * The oracle for time trial oracle.
   */
  @Test
  public void timeTrialOracleForced() {
    // don't run normally
    if (Main.DEBUG) {
      for (int i = 0; i < NUM_LOBBIES_TO_TEST; i++) {
        WikiGame game = getGame(TIME_TRIAL_GAME_PAGE_DIST);
        WikiLobby lobby =
            getLobby(WikiGameMode.Mode.TIME_TRIAL, NUM_TIME_TRIAL_GEN_PLAYERS,
                game);
        setupPlayersForce(lobby);
        // on the likely chance they weren't all ready
        if (!lobby.started()) {
          lobby.start(true);
        }
        assertTrue(lobby.started());
        playGame(lobby, game, this::checkTimeTrialEndState);
      }
    }
  }

  private void checkTimeTrialEndState(WikiLobby lobby) {
    for (WikiPlayer player : lobby.getAllPlayers()) {
      setDisconnectedWithProb(lobby, player, END_GAME_DISCONNECT_PROB);
    }

    assertTrue(lobby.started());
    assertTrue(lobby.ended());
    assertTrue(lobby.getWinners().size() == 1);
    assertTrue(lobby.checkForWinner());
    assertTrue(lobby.getWinners().size() == 1);

    System.out.println("\tWinner: " + lobby.getWinners());

    Instant endTime = lobby.getEndTime();
    for (WikiPlayer winner : lobby.getWinners()) {
      assertTrue(winner.done());
      assertTrue(
          winner.getCurPage().equalsAfterRedirectSafe(lobby.getGoalPage()));
      assertEquals(winner.getEndTime(), endTime);
    }

    for (WikiPlayer player : lobby.getAllPlayers()) {
      assertTrue(player.done());
      assertNotNull(player.getEndTime());
      assertTrue(player.getEndTime().isBefore(endTime)
          || player.getEndTime().equals(endTime));
    }
  }

  private static final int NUM_LEAST_CLICKS_GEN_PLAYERS = 3;
  private static final int LEAST_CLICKS_GAME_PAGE_DIST  = 1;

  /**
   * The oracle for least clicks game mode.
   */
  @Test
  public void leastClicksOracle() {
    // don't run normally
    if (Main.DEBUG) {
      for (int i = 0; i < NUM_LOBBIES_TO_TEST; i++) {
        WikiGame game = getGame(LEAST_CLICKS_GAME_PAGE_DIST);
        WikiLobby lobby =
            getLobby(WikiGameMode.Mode.LEAST_CLICKS,
                NUM_LEAST_CLICKS_GEN_PLAYERS, game);
        setupPlayers(lobby);
        // don't have to start because all ready
        assertTrue(lobby.started());
        playGame(lobby, game, this::checkLeastClicksEndState);
      }
    }
  }

  private void checkLeastClicksEndState(WikiLobby lobby) {
    for (WikiPlayer player : lobby.getAllPlayers()) {
      setDisconnectedWithProb(lobby, player, END_GAME_DISCONNECT_PROB);
    }

    assertTrue(lobby.started());
    assertTrue(lobby.ended());
    assertTrue(lobby.getWinners().size() > 0);
    assertTrue(lobby.checkForWinner());
    assertTrue(lobby.getWinners().size() > 0);

    System.out.println("\tWinners: " + lobby.getWinners());

    WikiPlayer prev = new ArrayList<>(lobby.getWinners()).get(0);
    for (WikiPlayer winner : lobby.getWinners()) {
      assertTrue(winner.done());
      assertTrue(
          winner.getCurPage().equalsAfterRedirectSafe(lobby.getGoalPage()));
      assertNotNull(winner.getEndTime());

      // make sure all have same path length
      assertEquals(prev.getPathLength(), winner.getPathLength());
      prev = winner;
    }

    final int shortestPath = prev.getPathLength();
    for (WikiPlayer player : lobby.getAllPlayers()) {
      assertTrue(player.done());
      assertTrue(
          player.getCurPage().equalsAfterRedirectSafe(lobby.getGoalPage()));
      assertTrue(player.getPathLength() >= shortestPath);
    }
  }

  private static final double INITIAL_DISCONNECT_PROB = 0.025; // this
  // has fewer
  // iterations
  private static final double IN_GAME_DISCONNECT_PROB  = 0.05;
  private static final double END_GAME_DISCONNECT_PROB = 0.1; // get
  // some
  // reconnects
  private static final int    MAX_TURNS              = 500;
  private static final double BAD_PLAYER_PAGE_PROB   = 0.075;
  private static final double PLAYER_GO_FORWARD_PROB = 0.9;
  private static final double PLAYER_GO_BACK_PROB    = 0.25; // not getting
                                                             // stuck
  private static final double GO_TO_VISITED_PROB     = 0.2;  // speed issue

  private void playGame(WikiLobby lobby, WikiGame game,
      Consumer<WikiLobby> endCb) {
    assertTrue(lobby.started());
    int turn = 0;
    while (turn < MAX_TURNS) {
      for (WikiPlayer player : lobby.getConnectedPlayers()) {
        assertTrue(lobby.started());
        // make sure to use duration overridden equals
        // assertTrue(lobby.getPlayTime().equals(player.getPlayTime())); //
        // TODO:?

        // disconnect some
        setDisconnectedWithProb(lobby, player, IN_GAME_DISCONNECT_PROB);
        // (may change state)
        if (lobby.ended()) {
          endCb.accept(lobby);
          return;
        }

        // for least clicks mode where it won't end
        if (!player.getCurPage().equalsAfterRedirectSafe(lobby.getGoalPage())) {
          if (withProb(PLAYER_GO_FORWARD_PROB)) {
            WikiPage prevPage = player.getCurPage();
            try {
              WikiPage pageToGoTo;
              if (withProb(BAD_PLAYER_PAGE_PROB)) {
                pageToGoTo = getRandomPage(game.getSpace());
              } else {
                // don't go to one's in path if we can help it
                do {
                  pageToGoTo = getRandomLink(player, game.getSpace());
                } while (player.getPath().contains(pageToGoTo)
                    && !withProb(GO_TO_VISITED_PROB)); // invert to break
              }

              // may need to consider other option here
              if (player.goToPage(pageToGoTo)) {
                assertTrue(
                    pageToGoTo.equalsAfterRedirectSafe(player.getCurPage()));
              } else {
                assertTrue(
                    prevPage.equalsAfterRedirectSafe(player.getCurPage()));
              }
            } catch (IOException e) {
              // just ignore
              assertTrue(prevPage.equalsAfterRedirectSafe(player.getCurPage()));
            }
          }

          // break on end
          if (lobby.ended()) {
            endCb.accept(lobby);
            return;
          }

          if (withProb(PLAYER_GO_BACK_PROB)) {
            WikiPage pageToGoBackTo;
            if (withProb(BAD_PLAYER_PAGE_PROB)) {
              pageToGoBackTo = getRandomPage(game.getSpace());
            } else {
              pageToGoBackTo =
                  player.getPath()
                      .get((int) (Math.random() * player.getPathLength()))
                      .getPage();
            }

            WikiPage prevPage = player.getCurPage();
            try {
              player.goBackPage(pageToGoBackTo);
              assertTrue(
                  pageToGoBackTo.equalsAfterRedirectSafe(player.getCurPage()));
            } catch (IOException e) {
              // just ignore
              assertTrue(prevPage.equalsAfterRedirectSafe(player.getCurPage()));
            } catch (NoSuchElementException e) {
              assertTrue(prevPage.equalsAfterRedirectSafe(player.getCurPage()));
            }
          }

          // shouldn't be able to end on a back
          assertTrue(!lobby.ended());
        }
      }
      turn++;
    }
    System.out.println("\tTurns maxed out");
    assertTrue(false);
  }

  /**
   * Helpers.
   */

  private void setDisconnectedWithProb(WikiLobby lobby, WikiPlayer player,
      double prob) {
    if (withProb(prob)) {
      lobby.playerDisconnected(player.getId());
    } else {
      lobby.playerReconnected(player.getId());
    }
  }

  private void setupPlayers(WikiLobby lobby) {
    // all initially connected
    for (WikiPlayer player : lobby.getConnectedPlayers()) {
      // randomly disconnect some
      player.setName(player.getId());
      setDisconnectedWithProb(lobby, player, INITIAL_DISCONNECT_PROB);
      player.setReady(true);
      assertTrue(player.ready());
    }
  }

  private static final double READY_PROBABILITY = 0.5;

  private void setupPlayersForce(WikiLobby lobby) {
    // all initially connected
    for (WikiPlayer player : lobby.getConnectedPlayers()) {
      // randomly disconnect and non-ready some
      player.setName(player.getId());
      setDisconnectedWithProb(lobby, player, INITIAL_DISCONNECT_PROB);
      player.setReady(withProb(READY_PROBABILITY));
    }
  }

  private WikiLobby getLobby(WikiGameMode.Mode mode, int numPlayers,
      WikiGame game) {
    // not necessary
    WikiLobby lobby = new WikiLobby(new FakeServer(), "testing-lobby", null);

    try {
      // difficulty doesn't matter
      lobby.init(buildObj(ImmutableMap.of("gameMode", mode.ordinal(),
          "difficulty", 0, "startPage", game.getStart().getName(), "goalPage",
          game.getGoal().getName())));
    } catch (InputError e) {
      throw new AssertionError("init failed");
    }
    generatePlayers(lobby, numPlayers);

    return lobby;
  }

  private List<String> generatePlayers(WikiLobby lobby, int num) {
    List<String> players = new ArrayList<>();
    for (int i = 0; i < num; i++) {
      String player = String.format("player-%d", i);
      lobby.addClient(player);
      players.add(player);
    }
    return players;
  }

  /**
   * @return true with probability
   */
  private boolean withProb(double probability) {
    return Math.random() <= probability;
  }

  /**
   * Page getters.
   */

  private static final double LOOKAHEAD_PROB = 0.75;

  private WikiPage getRandomLink(WikiPlayer player, Set<WikiPage> possible) {
    Set<WikiPage> links;
    try {
      links = WikiLobby.DEFAULT_LINK_FINDER.linkedPages(player.getCurPage());

      // NOTE: Ensure that virtual players get the link when the get on a page
      // with it
      for (WikiPage link : links) {
        // too slow to try for full equality
        if (player.getLobby().getGoalPage().equals(link)) {
          return link;
        }
      }

      for (WikiPage link : links) {
        if (possible.contains(link) && !withProb(LOOKAHEAD_PROB)) {
          return link;
        }
      }

    } catch (IOException e) {
      // do nothing
    }
    return player.getCurPage();
  }

  private WikiGame getGame(int ofDist) {
    return GameGenerator.ofDist(ofDist, true);
  }

  private WikiPage getRandomPage(Set<WikiPage> possible) {
    return new ArrayList<>(possible)
        .get((int) (Math.random() * possible.size()));
  }

  private WikiPage getRandomPage(WikiPage not, Set<WikiPage> possible) {
    return getRandomPage(ImmutableList.of(not), possible);
  }

  private WikiPage getRandomPage(Collection<WikiPage> nots,
      Set<WikiPage> possible) {
    WikiPage page;
    do {
      page = getRandomPage(possible);
    } while (nots.contains(page));
    return page;
  }

  private JsonObject buildObj(Map<String, ?> fields) {
    return Main.GSON.toJsonTree(fields).getAsJsonObject();
  }
}

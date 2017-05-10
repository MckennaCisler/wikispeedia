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
import edu.brown.cs.jmrs.web.wikipedia.FakeServer;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * An oracle tester to randomly generate games and runs them through WikiLobby.
 *
 * @author mcisler
 */
public class WikiGameOracle {
  private static final int NUM_LOBBIES_TO_TEST = 8;

  private static List<WikiPage> samplePages = new ArrayList<>();

  static {
    samplePages.add(WikiPage.fromName("Gilbert_Strang"));
    samplePages.add(WikiPage.fromName("Thai_solar_calendar"));
    samplePages.add(WikiPage.fromName("Collectivization_in_Romania"));
    samplePages.add(WikiPage.fromName("Charles_Dupuy"));
    samplePages
        .add(WikiPage.fromName("French_Maastricht_Treaty_referendum,_1992"));
    samplePages.add(WikiPage.fromName("Recep_Tayyip_Erdoğan"));
    samplePages.add(WikiPage.fromName("Hotak_dynasty"));
    samplePages.add(WikiPage.fromName("Cabinet_Office_Briefing_Room"));
    samplePages.add(WikiPage.fromName("Stephen_Mallory"));
    samplePages.add(WikiPage.fromName("Genuine_Risk"));
    samplePages.add(WikiPage.fromName("Porphyria"));
    samplePages
        .add(WikiPage.fromName("List_of_diplomatic_missions_in_Georgia"));
    samplePages.add(WikiPage.fromName("French_Open"));
    samplePages.add(WikiPage.fromName("Pakistani_cuisine"));
    samplePages.add(WikiPage.fromName("Reptiles"));
    samplePages.add(WikiPage.fromName("Spectrum_Sports,_Inc._v._McQuillan"));
    samplePages.add(WikiPage.fromName("Donn_Handicap"));
    samplePages.add(WikiPage.fromName("Demographics_of_Cuba"));
    samplePages.add(WikiPage.fromName("Don_Adams"));
    samplePages.add(WikiPage.fromName("Ghaznavids"));
    samplePages.add(
        WikiPage.fromName("Institute_of_Electrical_and_Electronics_Engineers"));
    samplePages.add(WikiPage.fromName("Dewan"));
    samplePages.add(WikiPage.fromName("501(c)(3)"));
    samplePages.add(WikiPage.fromName("Narasimhaiengar_Mukunda"));
    samplePages.add(WikiPage.fromName("Anti-Secession_Law"));
    samplePages.add(WikiPage.fromName("Soviet_war_in_Afghanistan"));
    samplePages.add(WikiPage.fromName("Governor_of_Nevada"));
    samplePages.add(WikiPage.fromName("Sainte-Foy,_Quebec_City"));
    samplePages.add(WikiPage.fromName("Al-Qata'i"));
    samplePages.add(WikiPage.fromName("Swale_(horse)"));
    samplePages.add(WikiPage.fromName("Doctoral_advisor"));
    samplePages.add(WikiPage.fromName("Vijay_Balakrishna_Shenoy"));
    samplePages.add(WikiPage.fromName("Cavalry_in_the_American_Civil_War"));
    samplePages.add(WikiPage.fromName("Automobile_Dacia"));
    samplePages.add(WikiPage.fromName("Formant"));
    samplePages.add(WikiPage.fromName("Albert_Smith_White"));
    samplePages.add(WikiPage.fromName("Georgian_art"));
    samplePages.add(WikiPage.fromName("Federal_War"));
    samplePages.add(WikiPage.fromName("Résistons!"));
    samplePages.add(WikiPage.fromName("University_of_Karachi"));
    samplePages.add(WikiPage.fromName("Washington_D.C."));
    samplePages.add(WikiPage.fromName("Neville_Chamberlain"));
    samplePages.add(WikiPage.fromName("Transportation_in_the_Philippines"));
    samplePages.add(WikiPage.fromName("Civil_Rights_Act_of_1875"));
    samplePages.add(WikiPage.fromName("Civil_Rights_Act_of_1866"));
    samplePages.add(WikiPage.fromName("Bărăgan_Plain"));
    samplePages.add(WikiPage.fromName("David_Lidington"));
    samplePages.add(
        WikiPage.fromName("List_of_U.S._counties_named_after_U.S._Presidents"));
    samplePages.add(WikiPage.fromName("Battle_of_Coamo"));
    samplePages.add(WikiPage.fromName("Baotou"));
    samplePages.add(WikiPage.fromName("Battle_of_Ponta_Delgada"));
    samplePages.add(WikiPage.fromName("Law_school"));
    samplePages.add(WikiPage.fromName("Henry_Bilson-Legge"));
    samplePages.add(WikiPage.fromName("2013–14_Tunisian_political_crisis"));
    samplePages.add(WikiPage.fromName("Guerrilla_warfare"));
    samplePages.add(WikiPage.fromName("Ben_Brush"));
    samplePages.add(WikiPage.fromName("Australia"));
    samplePages.add(WikiPage.fromName("Georgian_era"));
    samplePages.add(WikiPage.fromName("European_Single_Market"));
  }

  private static final int NUM_TIME_TRIAL_GEN_PLAYERS = 3;

  /**
   * The oracle for time trial oracle.
   */
  @Test
  public void timeTrialOracle() {
    for (int i = 0; i < NUM_LOBBIES_TO_TEST; i++) {
      WikiGame game = getGame();
      WikiLobby lobby =
          getLobby(WikiGameMode.Mode.TIME_TRIAL, NUM_TIME_TRIAL_GEN_PLAYERS,
              game);
      setupPlayers(lobby);
      // don't have to start because all ready
      assertTrue(lobby.started());
      playGame(lobby, game, this::checkTimeTrialEndState);
    }
  }

  /**
   * The oracle for time trial oracle.
   */
  @Test
  public void timeTrialOracleForced() {
    for (int i = 0; i < NUM_LOBBIES_TO_TEST; i++) {
      WikiGame game = getGame();
      WikiLobby lobby =
          getLobby(WikiGameMode.Mode.TIME_TRIAL, NUM_TIME_TRIAL_GEN_PLAYERS,
              game);
      setupPlayersForce(lobby);
      // watch for odd chance
      if (!lobby.started()) {
        lobby.start(true);
      }
      assertTrue(lobby.started());
      playGame(lobby, game, this::checkTimeTrialEndState);
    }
  }

  private void checkTimeTrialEndState(WikiLobby lobby) {
    // TODO Test players leaving?

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

  /**
   * The oracle for least clicks game mode.
   */
  @Test
  public void leastClicksOracle() {
    for (int i = 0; i < NUM_LOBBIES_TO_TEST; i++) {
      WikiGame game = getGame();
      WikiLobby lobby =
          getLobby(WikiGameMode.Mode.LEAST_CLICKS, NUM_LEAST_CLICKS_GEN_PLAYERS,
              game);
      setupPlayers(lobby);
      // don't have to start because all ready
      assertTrue(lobby.started());
      playGame(lobby, game, this::checkLeastClicksEndState);
    }
  }

  private void checkLeastClicksEndState(WikiLobby lobby) {
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

  private static final double INITIAL_DISCONNECT_PROB = 0.05; // this has fewer
                                                              // iterations
  private static final double IN_GAME_DISCONNECT_PROB = 0.02;
  private static final int    MAX_TURNS               = 500;
  private static final double PLAYER_GO_FORWARD_PROB  = 0.9;
  private static final double PLAYER_GO_BACK_PROB     = 0.15;
  private static final double BAD_PLAYER_PAGE_PROB    = 0.075;
  private static final double GO_TO_VISITED_PROB      = 0.1;  // speed issue

  private void playGame(WikiLobby lobby, WikiGame game,
      Consumer<WikiLobby> endCb) {
    int turn = 0;
    while (turn < MAX_TURNS) {
      for (WikiPlayer player : lobby.getConnectedPlayers()) {
        assertEquals(lobby.getPlayTime(), player.getPlayTime()); // TODO: !!!!

        // disconnect some
        setDisconnectedWithProb(lobby, player, IN_GAME_DISCONNECT_PROB);
        // (may change state)
        if (lobby.ended()) {
          endCb.accept(lobby);
          return;
        }

        if (withProb(PLAYER_GO_FORWARD_PROB)) {
          WikiPage prevPage = player.getCurPage();
          try {
            WikiPage pageToGoTo;
            if (withProb(BAD_PLAYER_PAGE_PROB)) {
              pageToGoTo = getRandomPage(game.getSpace());
            } else {
              // don't go to one's in path if we can help it
              do {
                pageToGoTo =
                    getRandomLink(player.getCurPage(), game.getSpace());
              } while (player.getPath().contains(pageToGoTo)
                  && !withProb(GO_TO_VISITED_PROB)); // invert to break
            }

            // may need to consider other option here
            if (player.goToPage(pageToGoTo)) {
              assertTrue(
                  pageToGoTo.equalsAfterRedirectSafe(player.getCurPage()));
            } else {
              assertTrue(prevPage.equalsAfterRedirectSafe(player.getCurPage()));
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

    System.out
        .println(
            String.format("Generated %s lobby with %d players (%s -> %s) :",
                mode.equals(WikiGameMode.Mode.TIME_TRIAL) ? "time trial"
                    : "least clicks",
                numPlayers, game.getStart(), game.getGoal()));

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

  private WikiPage getRandomLink(WikiPage curPage, Set<WikiPage> possible) {
    Set<WikiPage> links;
    try {
      links = WikiLobby.DEFAULT_LINK_FINDER.linkedPages(curPage);

      for (WikiPage link : links) {
        if (possible.contains(link)) {
          return link;
        }
      }
    } catch (IOException e) {
      // do nothing
    }
    return curPage;
  }

  private static final int GAME_PAGE_DIST = 5;

  private WikiGame getGame() {
    return GameGenerator.ofDist(GAME_PAGE_DIST, true);
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

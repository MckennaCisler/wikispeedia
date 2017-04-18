package edu.brown.cs.jmrs.wikispeedia;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;

import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * Coordinates a lobby of players in a Wiki game.
 *
 * @author mcisler
 *
 */
public class WikiLobby implements Lobby {
  /**
   * Global id available for next lobby. Each newly constructed lobby gets and
   * increments this.
   */
  // private static AtomicInteger nextLobbyId = new AtomicInteger(0);

  private final Server server;
  private final String id;
  private final Map<String, WikiPlayer> players; // from
                                                 // id
                                                 // to
                                                 // player
  private final LinkFinder<WikiPage> linkFinder;
  private Instant startTime = null;
  private Instant endTime = null;

  private WikiPage startPage;
  private WikiPage goalPage;

  private WikiPlayer winner;

  /**
   * Constructs a new WikiLobby (likely through a Factory in
   * {@link edu.brown.cs.jmrs.server.Server}.
   *
   * @param server
   *          The server it was called from.
   * @param id
   *          The id of this lobby.
   * @param linkFinder
   *          The linkFinder to use when showing / letting players move through
   *          pages.
   * @param startPage
   *          The starting page of players in this lobby.
   * @param goalPage
   *          The page players in this lobby are trying to get to.
   */
  public WikiLobby(Server server, String id, LinkFinder<WikiPage> linkFinder,
      WikiPage startPage, WikiPage goalPage) {
    this.server = server;
    this.id = id;
    players = new HashMap<>();
    this.linkFinder = linkFinder;
    this.startPage = startPage;
    this.goalPage = goalPage;
  }

  /****************************************/
  /* LOBBY OVERRIDES */
  /****************************************/

  @Override
  public boolean isClosed() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void addPlayer(String playerId) {
    if (started()) {
      throw new IllegalStateException(
          "Game has already started, cannot add player.");
    }
    this.players.put(playerId,
        new WikiPlayer(playerId, "TODO", this, startPage, goalPage)); // TODO:
  }

  // @Override
  public void removePlayer(String playerId) {
    this.players.remove(playerId);
  }

  /****************************************/
  /* STATE HANDLERS / SETTERS */
  /****************************************/

  /**
   * Start the game by setting a start time. Players cannot join after this.
   */
  public void start() {
    startTime = Instant.now();
  }

  /**
   * Stops the game by setting an end time and configuring all players.
   */
  public void stop() {
    endTime = Instant.now();
    for (Entry<String, WikiPlayer> entry : players.entrySet()) {
      if (!entry.getValue().isDone()) {
        entry.getValue().setEndTime(endTime);
      }
    }
  }

  /**
   * Checks for a winning player based on each's position. Should be called
   * periodically. Note that winners are not determine here, so the race
   * condition occurs elsewhere if two are close.
   */
  public void update() {
    List<WikiPlayer> done = new ArrayList<>(1);
    for (Entry<String, WikiPlayer> entry : players.entrySet()) {
      if (entry.getValue().isDone()) {
        done.add(entry.getValue());
      }
    }

    if (done.size() == 1) {
      winner = done.get(0);
      stop();

    } else if (done.size() > 1) {
      // sort by play time TODO: what about shortest path?
      done.sort((p1, p2) -> p1.getPlayTime().compareTo(p2.getPlayTime()));
      winner = done.get(0);
      stop();
    }
  }

  /****************************************/
  /* GETTERS */
  /****************************************/

  /**
   * @return The players in this map.
   */
  public List<WikiPlayer> getPlayers() {
    return ImmutableList.copyOf(players.values());
  }

  /**
   * @param playerId
   *          The id of the player to get.
   * @return The player with that ID, or null if none found.
   */
  public WikiPlayer getPlayer(String playerId) {
    return players.get(playerId);
  }

  /**
   * @return The player who won the game.
   * @throws IllegalStateException
   *           If the game has not ended.
   */
  public WikiPlayer getWinner() {
    if (!ended()) {
      throw new IllegalStateException("Lobby has not ended.");
    }
    return winner;
  }

  /**
   * @return Whether the lobby has started the game.
   */
  public boolean started() {
    return startTime != null;
  }

  /**
   * @return Whether the lobby has ended, either due to cancelling or a player
   *         winning.
   */
  public boolean ended() {
    return endTime != null;
  }

  /**
   * @return The current time the lobby has been started, i.e. the duration
   *         since startTime.
   * @throws IllegalStateException
   *           If the lobby has not been started.
   */
  public Duration getPlayTime() {
    if (!started()) {
      throw new IllegalStateException("Lobby not started.");
    }
    return Duration.between(startTime, Instant.now());
  }

  /**
   * @return When this lobby was started.
   * @throws IllegalStateException
   *           If the lobby has not been started.
   */
  public Instant getStartTime() {
    if (!started()) {
      throw new IllegalStateException("Lobby not started.");
    }
    return startTime;
  }

  /**
   * @return When this lobby ended.
   * @throws IllegalStateException
   *           If the lobby has not ended or was not started.
   */
  public Instant getEndTime() {
    if (!ended()) {
      throw new IllegalStateException("Lobby has not ended.");
    }
    return endTime;
  }

  /**
   * @return The start wiki page of this lobby.
   */
  public WikiPage getStartPage() {
    return startPage;
  }

  /**
   * @return The goal wiki page of this lobby.
   */
  public WikiPage getGoalPage() {
    return goalPage;
  }

  /**
   * @return The LinkFinder associated with this lobby, used in finding links
   *         from WikiPages.
   */
  public LinkFinder<WikiPage> getLinkFinder() {
    return linkFinder;
  }

}

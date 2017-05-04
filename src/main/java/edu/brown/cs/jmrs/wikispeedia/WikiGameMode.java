package edu.brown.cs.jmrs.wikispeedia;

import java.time.Instant;
import java.util.Set;

import edu.brown.cs.jmrs.web.ContentFormatter;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * An interface representing all the requirements of a game mode.
 *
 * @author mcisler
 *
 */
public interface WikiGameMode {
  /**
   * An ENUM of all game modes.
   *
   * @author mcisler
   *
   */
  enum Mode {
    TIME_TRIAL, LEAST_CLICKS
  }

  /**
   * @return The LinkFinder associated with this GameMode, used in finding links
   *         from WikiPages.
   */
  LinkFinder<WikiPage> getLinkFinder();

  /**
   * @return The ContentFormatter associated with this GameMode, used to
   *         reformat the parsedContent() of player's Wikipages.
   */
  ContentFormatter<WikiPage> getContentFormatter();

  /**
   * Checks for a winning player based on each's position in a lobby. Should be
   * called periodically, or at least at every player move.s
   *
   * @param lobby
   *          The lobby to check.
   * @return A set of all winners (in case of tie), whether they
   */
  Set<WikiPlayer> checkForWinners(WikiLobby lobby);

  /**
   * @param wikiLobby
   *          The lobby to get the end state of.
   * @return Whether the lobby has ended, based on its internal game mode.
   */
  boolean ended(WikiLobby wikiLobby);

  /**
   * @param wikiLobby
   *          The lobby to get the end time of.
   * @return When this lobby ended.
   * @throws IllegalStateException
   *           If the lobby has not ended or was not started.
   */
  Instant getEndTime(WikiLobby wikiLobby);

}

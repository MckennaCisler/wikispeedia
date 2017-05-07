package edu.brown.cs.jmrs.wikispeedia;

import java.time.Instant;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.brown.cs.jmrs.web.ContentFormatter;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * A time-based implementation of GameMode which defines a winning player as the
 * first one to finish.
 *
 * @author mcisler
 *
 */
public class TimeTrialGameMode implements WikiGameMode {
  @Override
  public Mode getGameMode() {
    return WikiGameMode.Mode.TIME_TRIAL;
  }

  @Override
  public LinkFinder<WikiPage> getLinkFinder() {
    return WikiLobby.DEFAULT_LINK_FINDER;
  }

  @Override
  public ContentFormatter<WikiPage> getContentFormatter() {
    return WikiLobby.DEFAULT_CONTENT_FORMATTER;
  }

  @Override
  public Set<WikiPlayer> checkForWinners(WikiLobby lobby) {
    // sort by play time
    Queue<WikiPlayer> done =
        new PriorityQueue<>(
            (p1, p2) -> p1.getPlayTime().compareTo(p2.getPlayTime()));
    for (WikiPlayer player : lobby.getPlayers()) {
      if (player.done()) {
        done.add(player);
      }
    }

    // only set if unset
    if (lobby.getWinners().size() == 0) {
      if (done.size() == 0) {
        return ImmutableSet.of();
      } else if (done.size() == 1) {
        return ImmutableSet.of(done.peek());
      } else {
        throw new IllegalStateException(
            "More than two people were done when checking for winners");
      }
    } else {
      return lobby.getWinners();
    }
  }

  @Override
  public boolean ended(WikiLobby wikiLobby) {
    // if any players is done
    for (WikiPlayer player : wikiLobby.getPlayers()) {
      if (player.done()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Instant getEndTime(WikiLobby wikiLobby) {
    if (!ended(wikiLobby)) {
      throw new IllegalStateException("Lobby has not ended.");
    }
    // get winner's end time
    if (wikiLobby.getWinners().size() == 0) {
      throw new AssertionError("Lobby was ended but no winner was defined.");
    }
    return new ArrayList<>(wikiLobby.getWinners()).get(0).getEndTime();
  }
}

package edu.brown.cs.jmrs.wikispeedia;

import java.time.Instant;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

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
  public static final WikiGameMode.Mode MODE = WikiGameMode.Mode.TIME_TRIAL;

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

    if (done.size() > 0) {
      if (done.size() > 1) {
        // remove all those that are not equal (in case this was called
        // after others are done)
        WikiPlayer first = done.poll();
        Set<WikiPlayer> winners = new HashSet<>();
        winners.add(first);

        while (first.getEndTime().equals(done.peek())) {
          winners.add(first);
          first = done.poll();
        }

        return winners;
      }
    }
    return new HashSet<>(done);
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
    for (WikiPlayer player : wikiLobby.getPlayers()) {
      if (wikiLobby.getWinners().equals(player)) {
        return player.getEndTime();
      }
    }
    throw new AssertionError("Lobby was ended but no winner was defined.");
  }
}

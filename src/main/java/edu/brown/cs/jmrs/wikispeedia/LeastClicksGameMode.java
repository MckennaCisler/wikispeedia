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
 * A path-length based implementation of GameMode, which defines a winning
 * player as the one with the shortest path at the end of the game.
 *
 * @author mcisler
 *
 */
public class LeastClicksGameMode implements WikiGameMode {

  @Override
  public WikiGameMode.Mode getGameMode() {
    return WikiGameMode.Mode.LEAST_CLICKS;
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
    // sort by path length
    Queue<WikiPlayer> done =
        new PriorityQueue<>((p1, p2) -> Integer.compare(p1.getPathLength(),
            p2.getPathLength()));
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
    // all players must be done
    for (WikiPlayer player : wikiLobby.getPlayers()) {
      if (player.connected() && !player.done()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Instant getEndTime(WikiLobby wikiLobby) {
    if (!ended(wikiLobby)) {
      throw new IllegalStateException("Lobby has not ended.");
    }

    // get last player's end time
    Instant laggardTime = wikiLobby.getPlayers().get(0).getEndTime();
    for (WikiPlayer player : wikiLobby.getPlayers()) {
      // note that lobby being ended implies all players are done.
      if (player.getEndTime().isAfter(laggardTime)) {
        laggardTime = player.getEndTime();
      }
    }
    return laggardTime;
  }

}

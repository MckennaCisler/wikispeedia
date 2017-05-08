package edu.brown.cs.jmrs.wikispeedia;

import java.time.Instant;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.brown.cs.jmrs.collect.Functional;
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
    if (ended(lobby)) {
      // this should be true when lobby is ended in this mode
      assert lobby.getConnectedPlayers().size() == Functional
          .filter(lobby.getConnectedPlayers(), WikiPlayer::done).size();

      // sort by path length
      Queue<WikiPlayer> done = getDoneSorted(lobby);

      // remove all those that are not equal (in case this was called
      // after others are done)
      WikiPlayer first = done.poll();
      Set<WikiPlayer> winners = new HashSet<>();

      WikiPlayer next = first;
      while (next != null && next.getPathLength() == first.getPathLength()) {
        winners.add(next);
        // reiterate
        next = done.poll();
      }
      return winners;
    }
    return ImmutableSet.of();
  }

  /**
   * @return Players that are done in this lobby, sorted by ascending path
   *         length.
   */
  private Queue<WikiPlayer> getDoneSorted(WikiLobby lobby) {
    Queue<WikiPlayer> done =
        new PriorityQueue<>((p1, p2) -> Integer.compare(p1.getPathLength(),
            p2.getPathLength()));

    done.addAll(
        Functional.filter(lobby.getConnectedPlayers(), WikiPlayer::done));
    return done;
  }

  @Override
  public boolean ended(WikiLobby wikiLobby) {
    // all players must be done, OR one player must have a shorter path than all
    // others
    Queue<WikiPlayer> done = getDoneSorted(wikiLobby);
    if (done.size() == wikiLobby.getConnectedPlayers().size()) {
      return true;
    } else if (done.size() > 0) {
      int shortestDone = done.peek().getPathLength();

      // if all players have longer (or the same) path lengths that the
      // one person who's done does, the game is done.
      // i.e. if any player's path length is less than the shortest one of the
      // done players, we need to keep going because they could still make it.
      for (WikiPlayer player : wikiLobby.getConnectedPlayers()) {
        if (player.getPathLength() < shortestDone) {
          return false;
        }
      }
      return true;
    }
    // no one is done
    return false;
  }

  @Override
  public Instant getEndTime(WikiLobby wikiLobby) {
    if (!ended(wikiLobby)) {
      throw new IllegalStateException("Lobby has not ended.");
    }

    // if there are no players (a completely expired lobby), just send null
    if (wikiLobby.getConnectedPlayers().size() > 0) {
      // get last player's end time
      assert wikiLobby.getConnectedPlayers().get(0).done();
      Instant laggardTime = wikiLobby.getConnectedPlayers().get(0).getEndTime();
      for (WikiPlayer player : wikiLobby.getConnectedPlayers()) {
        assert player.done();
        // note that lobby being ended implies all players are done.
        if (player.getEndTime().isAfter(laggardTime)) {
          laggardTime = player.getEndTime();
        }
      }
      return laggardTime;
    }
    return null;
  }
}

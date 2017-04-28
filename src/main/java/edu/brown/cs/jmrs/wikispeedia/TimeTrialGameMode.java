package edu.brown.cs.jmrs.wikispeedia;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import edu.brown.cs.jmrs.web.ContentFormatter;
import edu.brown.cs.jmrs.web.ContentFormatterChain;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiAnnotationRemover;
import edu.brown.cs.jmrs.web.wikipedia.WikiBodyFormatter;
import edu.brown.cs.jmrs.web.wikipedia.WikiFooterRemover;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.web.wikipedia.WikiPageLinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPageLinkFinder.Filter;

/**
 * A time-based implementation of GameMode which defines a winning player as the
 * first one to finish.
 *
 * @author mcisler
 *
 */
public class TimeTrialGameMode implements WikiGameMode {
  public static final WikiGameMode.Mode MODE = WikiGameMode.Mode.TIME_TRIAL;

  private static final LinkFinder<WikiPage> LINK_FINDER =
      new WikiPageLinkFinder(Filter.DISAMBIGUATION);

  private static final ContentFormatter<WikiPage> CONTENT_FORMATTER =
      new ContentFormatterChain<WikiPage>(
          ImmutableList.of(new WikiBodyFormatter(), new WikiFooterRemover(),
              new WikiAnnotationRemover()));

  @Override
  public LinkFinder<WikiPage> getLinkFinder() {
    return LINK_FINDER;
  }

  @Override
  public ContentFormatter<WikiPage> getContentFormatter() {
    return CONTENT_FORMATTER;
  }

  @Override
  public Optional<WikiPlayer> checkForWinner(WikiLobby lobby) {
    List<WikiPlayer> done = new ArrayList<>();
    for (WikiPlayer player : lobby.getPlayers()) {
      if (player.done()) {
        done.add(player);
      }
    }

    if (done.size() > 0) {
      if (done.size() > 1) {
        // sort by play time
        done.sort((p1, p2) -> p1.getPlayTime().compareTo(p2.getPlayTime()));
      }

      return Optional.of(done.get(0));
    }
    return Optional.absent();
  }

  @Override
  public boolean ended(WikiLobby wikiLobby) {
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
    for (WikiPlayer player : wikiLobby.getPlayers()) {
      if (wikiLobby.getWinner().equals(player)) {
        return player.getEndTime();
      }
    }
    throw new AssertionError("Lobby was ended but no winner was defined.");
  }
}

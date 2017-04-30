package edu.brown.cs.jmrs.wikispeedia;

import java.time.Instant;

import com.google.common.base.Optional;

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
  public static final WikiGameMode.Mode MODE = WikiGameMode.Mode.LEAST_CLICKS;

  @Override
  public LinkFinder<WikiPage> getLinkFinder() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ContentFormatter<WikiPage> getContentFormatter() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<WikiPlayer> checkForWinner(WikiLobby lobby) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean ended(WikiLobby wikiLobby) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Instant getEndTime(WikiLobby wikiLobby) {
    // TODO Auto-generated method stub
    return null;
  }

}

#### General todo
- <b>Fix bugs</b>
- Loading icons
- Least clicks mode
- Levels of difficulty
- Visualizations for history and endgame
- Chat everywhere
- Music
- Mobile-friendly
- Deployment
- Track history
- Shortest path
  - Create an EdgeFinder that caches edges
  - Create a web scraper to build the database
  - Integrate WikiGame into lobby
  - Add the shortest-path future to WikiGame
  - Make an intermitent (or parrallel... maybe do it while shortest-path finding?) DB-compare method that randomly double-checks a link list and changes it if needed
  - Make a two-sided Dijkstra?
- Design work
- Deal with what happens when a player can't be on a page (/waiting for ex.)
- Deal with ties (using "lists" of winners) for shortest-path mode
- Hide user's IDs from obvious places (i.e. ALL_PLAYERS) using isCurrentPlayer, etc.
- Add small REPL parser to hang main thread.

#### Deadlines:
- <b>Adversary TA Meeting (Wednesday, 4/26)</b>
- <b>Done with coding (Saturday, 5/6)</b>
- <b>Practice presentation w/ Erica (Sunday, 5/7)</b>
- <b>Demo (Monday, 5/8)</b>

#### Jacob

#### Rohan
- & in links (i.e. R&B)
- Geographic coordinates system
- Make videos work?

#### Sean

#### Mckenna
Core Functionality
  Core
      TIMING
          When to start (5s countdown in beginning, etc.)

Wiki Pages
    Be explicit with variable names (page name vs. href!)
    Deal with redirects with URL!!! (hascode, equals, etc.!)
    Change link parsing to use input stream (as an OPTION?)
    DEAL WITH THE FACT THAT WIKIPEDIA HAS A RATE LIMIT
    Use Trie for link storage?
    Graph Library
        Graph Searching
             edge weight / A* heuristic?
                conceptual distance?
                some function of the number of shared links between pages?
            Rewrite Dijkstra with DijkstraNode / PathObj abstraction

Testing
    Test HTML parsing across many webpages
WikiPlayer, WikiLobby game logic; WikIInterpreter too

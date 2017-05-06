#### General todo
##### NECESSITIES
- <b>Fix bugs</b>
  - BLOCKING 1000 Bug
- Visualizations for history and endgame ROHAN
- Deal with what happens when a player can't be on a page (/waiting for ex.)
  - Add a "close error" callback
- Going back (history) ROHAN
- Documentation
  - Stuff in docs folder
  - Demo Presentation
  - Difficulties / challenges
    - Mckennna - Race condition
    - Sean - Cookies / websockets?
    - Jacob - Attractive interface
    - Rohan - Visualizaton?
  - Wikipedia RATE LIMIT
##### NICETIES
- Shortest path
  - Add the shortest-path future to WikiGame
  - Make a two-sided Dijkstra?
- Deployment
##### STUFF THAT WOULD GIVE US THAT WARM FUZZY FEELING
- Make an intermittent (or parallel... maybe do it while shortest-path finding?) DB-compare method that randomly double-checks a link list and changes it if needed
- Music
- Hide user's IDs from obvious places (i.e. ALL_PLAYERS) using isCurrentPlayer, etc.
- Chat everywhere? SEAN

#### Deadlines:
- <b>Adversary TA Meeting (Wednesday, 4/26)</b>
- <b>Done with coding (Saturday, 5/6)<  /b>
- <b>Practice presentation w/ Erica (Sunday, 5/7)</b>
- <b>Demo (Monday, 5/8)</b>

#### Jacob

#### Rohan
- & in links (i.e. R&B)
- Geographic coordinates system
- Make videos work?

#### Sean

#### Mckenna
Wiki Pages
    Be explicit with variable names (page name vs. href!)
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

### Post demo
 - Have a preset path
 - Talk less about difficulties
 - More confidence
 - Practice

#### General todo
##### Documentation
- Stuff in docs folder
##### NECESSITIES
- <b>Fix bugs</b>
  - Add player timeout on disconnect so the game won't end (in shortest path for ex. ) as soon as the one who may still win disconnects
- Should a lobby really end when all players
##### NICETIES`
- Shortest path
  - Add intelligent switcher if DB busy?
  - Fix insane caching sizes (7000???)
  - Make queue act like a set with .equals?
  - Add the shortest-path future to WikiGame
  - Make a two-sided Dijkstra?
##### STUFF THAT WOULD GIVE US THAT WARM FUZZY FEELING
- Hide user's IDs from obvious places (i.e. ALL_PLAYERS) using isCurrentPlayer, etc.

#### Deadlines:
- <b>Adversary TA Meeting (Wednesday, 4/26)</b>
- <b>Done with coding (Saturday, 5/6)<  /b>
- <b>Practice presentation w/ Erica (Sunday, 5/7)</b>
- <b>Demo (Monday, 5/8)</b>

#### Jacob

#### Rohan
- & in links (i.e. R&B)
- Geographic coordinates system

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

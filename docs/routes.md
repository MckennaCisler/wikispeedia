## Routes

#### Lobby:

- -> List of available games
- Settings and players in a game -> Start and end articles

#### Game:

###### Phase 1 (Time-based):

- Article name -> HTML to render the article
- Article name -> List of links
- Player name (or ID?) -> History of the player (list of articles)
- -> Current time

###### Phase 2 (Shortest path):

- Nothing else?

#### End-game:
- Player name (or ID?) -> History of the player (list of articles)
- Player name (or ID?) -> Time it took to finish (time-based)
- Player name (or ID?) -> Length of path (shortest path)
- -> Shortest path

------
#### Central objects:
- Lobby/game: name, settings, state, list of players
- Player: names, lobby, path
- Path (list of nodes): some path through articles
- Articles (node): title, lists of articles

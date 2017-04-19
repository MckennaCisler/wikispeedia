## Routes

#### Lobby:

- List of available games (just names)
- -> list ${games}

#### Waiting:
- Settings and players in a game -> Start and end articles
- -> list ${players}
- -> ${startArticle}
- -> ${endArticle}
- -> -> See practiceHtml for desired formatting of the articles

#### Game:

###### Phase 1 (Time-based):

- articleBody -> HTML to render the article
- articleLinks -> List of links
- Player ID -> History of the player (list of articles and times spent on each one) ${player}
- -> Current time ${time}

###### Phase 2 (Shortest path):

- Nothing else?

#### End-game:
- Player ID -> History of the player (list of articles)
- Player ID -> Time it took to finish (time-based)
- Player ID -> Length of path (shortest path)
- -> Shortest path
- -> These are all needed for all players: list ${players}

------
#### Central objects:
- Lobby/game: name, settings, state, list of players
- Player: names, lobby, path
- Path (list of nodes): some path through articles
- Articles (node): title, lists of articles

## Flow (front-end vs. back-end)

This document functions like the "routes" in story form.

#### Lobby
1. All active games are requested from the backend.
2. If a new game is created, its settings are sent to the backend.
3. When a game is joined, the addition of the new player is sent to the backend.
4. When the game is started, the backend delivers the start and end articles. Games are started when the game's creator choose to start it.

#### Game
1. When an article is clicked on, the content and all of the links are queried from the backend. This renders the main page.
2. The history of every player is queried from the backend. This renders the game progress.

#### End game
1. The history of every player is queried from the backend.
2. The "results" are queried from the backend (time or path length).
3. The actual shortest path is queried from the backend.

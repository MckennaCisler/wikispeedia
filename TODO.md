# Jacob


# Rohan
- Styling of ul
- & in links (i.e. R&B)
- Geographic coordinates system
- Make videos work






# Sean

# Mckenna
Core Functionality
    Write Serializers
        Replace JsonSerializable with https://github.com/google/gson/blob/master/UserGuide.md#TOC-Custom-Serialization-and-Deserialization
    Core
        Change commands to transmit in uppercase?
        Change websockets so we don't need polling
            Send out the LOBBIES message to everyone when one is added
        TIMING
            When to start (5s countdown in beginning, etc.)
            When to update on /play?
            Should we send a message every second?
        Figure out how to nicely deserialize wikipages differently in different contexts

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

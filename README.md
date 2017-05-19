# Wikispeedia
### A beautiful Wikipedia game
_A multiplayer web app for the Wikipedia Game complete with a multiuser lobby system, game generation and database backend_

Check it out at [http://wikispeedia.herokuapp.com](http://wikispeedia.herokuapp.com)

**Team Members:**
Rohan Jha, Jacob Leiken, Sean Hastings, McKenna Cisler

This project began as a term project for Brown University's _CSCI 0320 Introduction to Software Engineering_.

## How to Build and Run
1. Make sure some version of `wikipedia.sqlite3` is in `data/`. A blank sqlite3 database will be initialized correctly and populated by the program. (_A database is included in the repository_)
2. Run `mvn package`
3. Run `./run` with any of the following options:
  - `--gui` To run the main WebSocket for the GUI
  - `--chat-test` To run the chat-test WebSocket for testing purposes. Do not run along with `--gui`
  - `--spark-port <port>` To set the port of the `--spark` server.
  - `--scrape` To simply scrape Wikipedia and populate database caches
  - `--scrape-depth <depth>` The depth at which to stop scraping. Defaults to infinity.
  - `--scrape-start <wiki page title>` The page to start from when scraping. Defaults to "Main_Page"
  - `--scrape-method <breadth | random_descent>` The method to use while scraping; breadth will try every page at every depth starting from start, while random descent will go depth-first, choosing a random link at each level.
  - `--scrape-only-english` Limits links to those in the English Wikipedia
  - `--shortest-path --start <wiki page title> --end <wiki page title>` Finds the shortest path between `start` and `end` using Dijkstra. WARNING: This will be very slow and will very aggressively scrape Wikipedia.
4. For debugging/HTML editing with instant refresh or verbose logging, ensure `DEBUG` or `VERBOSE_LOG` in `src/main/java/edu/brown/cs/jrms/ui/Main.java` is set to the appropriate value.

# Project Details
**Original Project Idea:**
A multiplayer, graphical implementation of the _Wikipedia game_ where the players
click on links to navigate from a _starting page_ to an _ending page_ on Wikipedia.

## Project Requirements
### Pre-game:

The users can quickly create and join games.
<br>_Rationale_: We previously had the idea of a dining hall recommender, and for that project, we conducted a survey with one question that asked about the setup process. More than half of the forty-nine respondents answered that they'd only be willing to tolerate a minute or less of setup. And in one interview we conducted, the interviewee said that all he wanted from the game creation page was an easy and clear way to get started.

A page with advanced settings for the game. _Ex: difficulty, sound,
etc._
<br>_Rationale_: There are many different ways to play the Wikipedia game, and we'd like to make all of them possible. Two interviewees also emphasized customizability in their responses.

The users are presented the _starting_ page's title and the _ending_ page's title.
The interface does not necessarily have the text of the pages themselves, but
it does have visible links that are ordered somehow.
<br>_Rationale_: The user should know their goal at all times. Further, an interviewee said that if page had hundreds of links it would be overwhelming for them to be scattered across the page.

### In-game:

A user-interface that graphically represents a Wikipedia page and its outgoing
links. The interface does not necessarily have the text of the page itself, but
it does have a visible link.
<br>_Rationale_: One interviewee stressed that she wanted the ability to read the articles and "learn cool stuff" while she played the game. We think that the presence of real articles would enrich the game and make it a stronger alternative to the real "Wikipedia game."

The users can easily move from one page to another. _Ex: visualize the
pages as towns and the links as roads._
<br>_Rationale_: This game is centered on exploration.

The users can track their progress and the progress of their opponents.
<br>_Rationale_: Interviewees differed in the extent to which they wanted to know their progress, but they all wanted some indication of their history. One interviewee wanted to see the distance of their opponent from the target. The other didn't, so this could be a setting.

The users can end the game midway.
<br>_Rationale_: If the user is stuck or bored, they should be able to start another game without having to leave the site.

### Post-game:

A page where the players' results are presented. This includes the players'
histories and a clear indication of the winner.
<br>_Rationale_: The interviewees talked about looking back at the game and the path they took from the start to the finish. One interviewee also talked about having "cool stats" in the final page for more insight about the game. The winner should be clearly displayed to promote the game's competitive element.

The users are presented with the shortest path from the _starting_ page to the
_ending_ page.
<br>_Rationale_: All the interviewees thought that this would be really interesting information, and they were excited about the possibility of comparing their route with the shortest.

The users can navigate to a page where new games can be joined or created.
<br>_Rationale_: Users should be able to play again.

### Extra features:

- Single-player and multiplayer modes.
- Exclusion of certain articles.
- Game modes with different levels of difficulty.
- A fairness judge (i.e. distance / weighted "difficulty" distance measure) to ensure fair random placement.
- In-game music.
- The users can see the path of the other users if it overlaps with their path.
- The users can save different configurations.
- The users can see the distance to the _ending_ page.
- Some visualization of the Wikipedia graph and the search algorithms (not clearly defined) (in-game).
- A visualization of the paths that were taken and the shortest paths (post-game).
- A statistical analysis of a player's saved games to analyze their decision-making and word/concept associations.
- A change in the graphical representation of a webpage based on the subject and/or length and/or distance of the page.

## Project Specs and Mockup

### Basic functionality:
 - Creating and joining a lobby with friends
 - One game type
 - Basic Wikipedia text, links
 - No advanced options
 - Some depiction of winner

### Great:
 - All advanced options
 - Full visualization, graphics
 - Lobby with option to join random game
 - Lobby chatting?

## Project Design Details
The following files are relevant (all in the docs subfolder):
- roles-and-deadlines.md
- routes.md
- flow.md
- testing.md
- expected-problems.md

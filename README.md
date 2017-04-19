# cs0320 Term Project

**Team Members:**
Rohan Jha, Jacob Leiken, Sean Hastings, McKenna Cisler

**Project Idea:**
A multiplayer, graphical implementation of the _Wikipedia game_ where the players
click on links to navigate from a _starting page_ to an _ending page_ on Wikipedia.

**Mentor TA:** Erica Oh (erica_oh@brown.edu)

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

### Still left to define:

- What's the lobby system?
- When does the game end?
- How are winners determined?

## Project Specs and Mockup

### Basic functionality:
 - Creating and joining a lobbby with friends
 - One game type
 - Basic Wikipedia text, links
 - No advanced options
 - Some depiction of winner

### Great:
 - All advanced options
 - Full visualization, graphics
 - Lobby with option to join random game
 - Lobby chatting?

## Project Design Presentation
The following files are relevant (all in the docs subfolder):
- roles-and-deadlines.md
- routes.md
- flow.md
- testing.md

TODO:
- Package design: packages, classes, methods
- Testing plan
- Timelines
- Expected problems

## How to Build and Run
_A necessary part of any README!_

## How to use server 'library'

#### command and response structure
All commands to the server must be JSON with a 'command' field. For built-ins this field defines the basic functionality desired by the client, and it is recommended that lobby systems built on the framework follow this convention.

Responses and messages to the client from the built-ins are also JSON, and each has a 'type' field denoting the meaning or context of the message and an 'error' field where the value is a short description of any error that occurred, or an empty string if no errors occurred. This pattern is also recommended to be followed for standardization of communication between the client and server.

#### built-in commands
All built-in commands send a response to the client with a field "error" which is an empty string if the operation succeeded and a short description of the error if not. None of these commands are case sensitive, meaning that commands only differing from these in case cannot be used as one's own commands.

##### Command List
- **set\_player\_id:** sets the id of the client which sent the message
    - \{ "command":"set\_player\_id", "player_id": *put ID here* \}
- **start\_lobby:** starts up a new lobby and automatically adds the client that sent the command. If an arguments field is inlcuded with the command json then the arguments will be passed to lobby.init().
    - \{ "command":"start\_lobby", "lobby_id": *put ID here* , (optional) "arguments": *json object of arguments* \}
- **leave\_lobby:** removes the client from it's current lobby if it is in one
    - \{ "command":"leave\_lobby" \}
- **join\_lobby:** adds the client to specified lobby
    - \{ "command":"join\_lobby", "lobby_id": *put ID here* \}
- **get\_lobbies:** retrieves a list of the ids of all open lobbies. Will eventually return json of lobby rather than just id.
    - \{ "command":"get\_lobbies" \}
    - response: \{ "error":"", "lobbies": *json array of lobby ids* \}

#### server.customizable
This is the bread and butter of the 'library'. To make a something that runs on this platform you have to make an implementation of CommandInterpreter and Lobby to manage your game. The command interpreter has to have a constructor that takes a Lobby and a player ID so the factory can properly instance it, or if you make your own factory to pass in that uses any arguments then there must be a constructor that has the aforementioned arguments after the ones defined in the creation of the factory.

The same applies to Lobby, except the required arguments are a Server and a lobby ID

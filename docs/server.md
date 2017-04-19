## How to Use the Server 'Library'

### Command and response structure:
All commands to the server must be JSON with a 'command' field. For built-ins this field defines the basic functionality desired by the client, and it is recommended that lobby systems built on the framework follow this convention.

Responses and messages to the client from the built-ins are also JSON, and each has a 'type' field denoting the meaning or context of the message and an 'error' field where the value is a short description of any error that occurred, or an empty string if no errors occurred. This pattern is also recommended to be followed for standardization of communication between the client and server.

### Built-in commands:
All built-in commands send a response to the client with a field "error" which is an empty string if the operation succeeded and a short description of the error if not. None of these commands are case sensitive, meaning that commands only differing from these in case cannot be used as one's own commands.

##### Command List:
- `set\_player\_id`: sets the id of the client which sent the message
    - \{ "command":"set\_player\_id", "player_id": *put ID here* \}
- `start\_lobby`: starts up a new lobby and automatically adds the client that sent the command. If an arguments field is inlcuded with the command json then the arguments will be passed to lobby.init().
    - \{ "command":"start\_lobby", "lobby_id": *put ID here* , (optional) "arguments": *json object of arguments* \}
- `leave\_lobby`: removes the client from it's current lobby if it is in one
    - \{ "command":"leave\_lobby" \}
- `join\_lobby`: adds the client to specified lobby
    - \{ "command":"join\_lobby", "lobby_id": *put ID here* \}
- `get\_lobbies`: retrieves a list of the ids of all open lobbies. Will eventually return json of lobby rather than just id.
    - \{ "command":"get\_lobbies" \}
    - response: \{ "error":"", "lobbies": *json array of lobby ids* \}

### Server.customizable:
This is the bread and butter of the 'library'. To make a something that runs on this platform you have to make an implementation of CommandInterpreter and Lobby to manage your game. The command interpreter has to have a constructor that takes a Lobby and a player ID so the factory can properly instance it, or if you make your own factory to pass in that uses any arguments then there must be a constructor that has the aforementioned arguments after the ones defined in the creation of the factory.

The same applies to Lobby, except the required arguments are a Server and a lobby ID.

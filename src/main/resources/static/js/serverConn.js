/**
 * Globals
 */
 var serverConn;

// Note that "INCOMING" and "OUTGOING" refer to the server perspective
const COMMAND_TYPE = {
  INCOMING: 0,
  RESPONSE: 1,
  OUTGOING: 2,
  SERVER: 3
};

const Command = {
    /**
     * Helpful regex:
     * ([A-Z_]+)\((\".*\")\, (COMMAND_TYPE\.\w+)\, \"(.*)\"\)(,//|;)
     * ->
     * $1 : { \n\t\tname: $2, \n\t\ttype: $3,\n\t\tconstruct: ($4) => {}\n\t},
     * (You'll need to do some manual changes... it's not the same on both sides)
     */

     /**
      * Default Server Commands
      */
    SET_CLIENT_ID: {
      name: "set_client_id",
      responseName: "set_id_response",
      type: COMMAND_TYPE.SERVER,
      construct: (client_id) => {
          return { "client_id": client_id };
      }
    },
    START_LOBBY: {
      name: "start_lobby",
      responseName: "start_lobby_response",
      type: COMMAND_TYPE.SERVER,
      construct: (lobby_id) => {
          return { "lobby_id" : lobby_id };
      }
    },
    LEAVE_LOBBY: {
      name: "leave_lobby",
      responseName: "leave_lobby_response",
      type: COMMAND_TYPE.SERVER,
      construct: () => { return "" }
    },
    JOIN_LOBBY: {
      name: "join_lobby",
      responseName: "join_lobby_response",
      type: COMMAND_TYPE.SERVER,
      construct: (lobby_id) => {
          return { "lobby_id" : lobby_id };
      }
    },
    GET_LOBBIES: {
      name: "get_lobbies",
      responseName: "get_lobbies_response",
      type: COMMAND_TYPE.SERVER,
      construct: () => { return "" }
    },
    COMMAND_ERROR: {
        name: "command_error",
        type: COMMAND_TYPE.SERVER, // but OUTGOING really
    },

    /**
     * INCOMING Commands.
     */
    // Lobby-specific commands
    GET_PLAYERS : {
        name: "get_players",
        responseName: "return_players",
        type: COMMAND_TYPE.INCOMING,
        construct: (lobby_id) => {
            return { "lobby_id" : lobby_id };
        }
    },
    GET_TIME: {
        name: "get_time",
        responseName: "return_time",
        type: COMMAND_TYPE.INCOMING,
        construct: () => { return "" }
    },
    GET_SETTINGS : {
		name: "get_settings",
        responseName: "return_settings",
		type: COMMAND_TYPE.INCOMING,
		construct: (lobby_id) => {
            return { "lobby_id" : lobby_id };
        }
	},
    // Player-specific commands
    GOTO_PAGE : {
		name: "goto_page",
        responseName: "return_page",
		type: COMMAND_TYPE.INCOMING,
		construct: (page_name) => {
            return { "page_name" : page_name };
        }
	},
    GET_PATH : {
		name: "get_path",
        responseName: "return_path",
		type: COMMAND_TYPE.INCOMING,
        construct: (player_id) => {
            return {"player_id" : player_id}
        }
	},
    /**
     * RESPONSEs to INCOMING Commands. (MOST are stored as references in the GET_ objects)
     */
    // Lobby-specific commands
    ERROR : {
		name: "error",
		type: COMMAND_TYPE.RESPONSE,
	},

    /**
     * OUTGOING Server Commands.
     */
    END_GAME : {
		name: "end_game",
		type: COMMAND_TYPE.OUTGOING,
	}
}

class ServerConn {
    constructor(source, clientId) {
        // constants
        this.COMMAND_TIMEOUT = 20000;

        this.clientId = clientId;
        this.ws = new WebSocket("ws://" + source);
        this.ws.onopen = this.ws_onopen.bind(this);
        this.ws.onmessage = this.ws_onmessage.bind(this);
        this.ws.onclose = this.ws_onclose.bind(this);

        /**
         * Map from Command name to an object of
         * the callback to call when such a message is recieved (with the parsed results), the errCallback to call on error, and a timeout to cancel on call.
         // TODO: What if the message returns with a DIFFERENT palyer id?
         */
        this.pendingResponses = {};
    }

    /**
     * Exposed functions
     */
    getPlayers(lobby_id, callback, errCallback) {
        this._send(Command.GET_PLAYERS, callback, errCallback, [lobby_id]);
    }

    getTime(callback, errCallback) {
        this._send(Command.GET_TIME, callback, errCallback, []);
    }

    getSettings(lobby_id, callback, errCallback) {
        this._send(Command.GET_SETTINGS, callback, errCallback, [lobby_id]);
    }

    gotoPage(page_name, callback, errCallback) {
        this._send(Command.GOTO_PAGE, callback, errCallback, [page_name]);
    }

    getPath(player_id, callback, errCallback) {
        this._send(Command.GET_PATH, callback, errCallback, [player_id]);
    }

    /**
     * default lobby SYSTEM commands
     */
    // set player_id to null or undefined to generate a random one
    setPlayerId(player_id, callback, errCallback) {
        this._send(Command.SET_CLIENT_ID, callback, errCallback, [player_id]);
    }

    // set lobby_id to null or undefined to generate a random one
    startLobby(lobby_id, callback, errCallback) {
        this._send(Command.START_LOBBY, callback, errCallback, [lobby_id]);
    }

    leaveLobby(callback, errCallback) {
        this._send(Command.LEAVE_LOBBY, callback, errCallback, []);
    }

    joinLobby(lobby_id, callback, errCallback) {
        this._send(Command.JOIN_LOBBY, callback, errCallback, [lobby_id]);
    }

    getLobbies(callback, errCallback) {
        this._send(Command.GET_LOBBIES, callback, errCallback, []);
    }

    /**
     * Register a specific handler for the END_GAME message. Note that the handler is erased after every END_GAME call.
     */
    registerEndgame(callback) {

        // TODO: What happens on a new page????????????????? (Jacob maybe does this somewhere?)

        this.pendingResponses[Command.END_GAME.name] = {
            "callback": callback,
            "errCallback" : () => {},
            "timeout" : null
        }
    }

    /**
     * Primary WebSocket interpreters
     */
    ws_onopen() {
		// This will set an initial client ID to a random value, it can be overwritten by sending a new ID later
        this.setPlayerId("", (parsedMsg) => {
            this.clientId = parsedMsg.client_id
        }, () => {});
        // TODO: What about sessions?

        // TODO: This
        // TODO: is just
        // TOOD: temporary
        this.startLobby("HARD CODED CRAZY LIT LOBBY", () => console.log("Lobby started"), () => console.log("Lobby failed to start"));
    };

    ws_onmessage(jsonMsg) {
        const parsedMsg = JSON.parse(jsonMsg.data);

        console.log("RECIEVING: ");
        console.log(parsedMsg);

        // if (this.clientId === undefined || // if we are getting and ID now
        //     parsedMsg.client_id === this.clientId) {
        if (this.pendingResponses.hasOwnProperty(parsedMsg.command)) {
            // note we can't really localize an error command to a player
            if (parsedMsg.command === Command.ERROR.name ||
                parsedMsg.command === Command.COMMAND_ERROR.name) {
                console.log("\nGot ERROR or COMMAND_ERROR: ");
                console.log(parsedMsg);

            } else if (parsedMsg.error_message === "") {
                // note that pendingResponses is a map from command RETURN MESSGAE NAME to the callbacks
                // that should be called on the return message, so we can reference these all in one line
                // without a switch statement
                const actions = this.pendingResponses[parsedMsg.command];
                actions.callback(parsedMsg);
                window.clearTimeout(actions.timeout);
            } else {
                const actions = this.pendingResponses[parsedMsg.command];
                actions.errCallback(parsedMsg);
                window.clearTimeout(actions.timeout);

                console.log("\nGot error: ");
                console.log(parsedMsg);
            }
        } else {
            console.log("\nUnknown command recieved: ");
            console.log(parsedMsg);
        }
        // }
    }

    ws_onclose() {
        // TODO : what in the world to do?
    }

    _send(command, callback, errCallback, args) {
        if (command.type !== COMMAND_TYPE.INCOMING &&
            command.type !== COMMAND_TYPE.SERVER) {
            console.log("ERROR: Wrong message type for sending in: ");
            console.log(command);
        }

        let msg = {
            "command": command.name,
            // apply with list of args to global (window "this" content)
            "payload": command.construct.apply(null, args)
        };

        console.log("SENDING: ");
        console.log(msg);

        // add an entry for the RESPONSE command to this one
        this.pendingResponses[command.responseName] = {
            "callback": callback,
            "errCallback": errCallback,
            "timeout": window.setTimeout(() => {
                if (errCallback != undefined) {
                    errCallback("Request timed out");
                }
                console.log("Response for " + command.name + " timed out.");
            }, this.COMMAND_TIMEOUT)
        };

        this.ws.send(JSON.stringify(msg));
    }
}

$(document).ready(() => {
    // global
    serverConn = new ServerConn("localhost:4568");
})

/* jshint esversion: 6 */
/**
 * Globals (created at bottom)
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
     * ([A-Z_]+)\((\".*\")\, (CommandType\.\w+)\, \"(.*)\"\)(, //|;)
     * ->
     * $1 : { \n\t\tname: $2, \n\t\ttype: $3,\n\t\tconstruct: ($4) => {}\n\t},
     * (You'll need to do some manual changes... it's not the same on both sides)
     */

     /**
      * Default Server Commands
      */
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
      responseName: "all_lobbies",
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
    FORCE_BEGIN_GAME: {
        name: "force_begin_game",
        responseName: "begin_game",
        type: COMMAND_TYPE.INCOMING,
        construct: () => { return "" }
    },
    GET_PAGE : {
		name: "get_page",
        responseName: "return_get_page",
		type: COMMAND_TYPE.INCOMING,
		construct: (page_name) => {
            return { "page_name" : page_name };
        }
	},
    // Player-specific commands
    SET_USERNAME : {
		name: "set_username",
        responseName: "return_set_username",
		type: COMMAND_TYPE.INCOMING,
		construct: (username) => {
            return {"username" : username };
        }
	},
    SET_PLAYER_STATE : {
		name: "set_player_state",
        responseName: "return_set_player_state",
		type: COMMAND_TYPE.INCOMING,
		construct: (state) => {
            return {"state" : state };
        }
	},
    GOTO_PAGE : {
		name: "goto_page",
        responseName: "return_goto_page",
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
    ALL_LOBBIES: {
   		name: "all_lobbies",
   		type: COMMAND_TYPE.OUTGOING,
   	},
    ALL_PLAYERS: {
  		name: "all_players",
  		type: COMMAND_TYPE.OUTGOING,
  	},
    BEGIN_GAME : {
 		name: "begin_game",
 		type: COMMAND_TYPE.OUTGOING,
 	},
    END_GAME : {
		name: "end_game",
		type: COMMAND_TYPE.OUTGOING,
	}
}

class ServerConn {
    constructor(source) {
        // constants
        this.COMMAND_TIMEOUT = 5000;
        this.CLIENT_ID_COOKIE_EXPIRATION = 60;

        this.clientId = "";
        this.ws = new WebSocket("ws://" + source);
        this.ws.onopen = this.ws_onopen.bind(this);
        this.ws.onmessage = this.ws_onmessage.bind(this);
        this.ws.onclose = this.ws_onclose.bind(this);

        /**
         * Map from Command name to an object of
         * the callback to call when such a message is recieved (with the parsed results), the errCallback to call on error, and a timeout to cancel on call.
         */
        this.pendingResponses = {};

        // add initial callback for first ID set
        this.pendingResponses["notify_id"] =  {
            "command": { type: COMMAND_TYPE.SERVER }, // all we need
            "callback": (message) => {
                this._setId(message.client_id);
            },
            "errCallback" : () => {}, // no reason
            "timeout" : null          // no reason
        }


        /**
         * callbacks called once websocket is ready
         */
        this.readyCallbacks = [];
    }

    _setId(id) {
        let d = new Date();
        d.setTime(d.getTime() + (this.CLIENT_ID_COOKIE_EXPIRATION * 60 * 1000)); //60 minutes
        let expires = "expires="+d.toUTCString();
        document.cookie = "client_id=" + id + d.getTime() + ";" + expires;
        this.clientId = id;
    }

    /**
     * Exposed functions; callbacks are called with relevant payload
     */
    // set lobby_id to "" for the current one
    getPlayers(lobby_id, callback, errCallback) {
        this._send(Command.GET_PLAYERS, callback, errCallback, [lobby_id]);
    }

    getTime(callback, errCallback) {
        this._send(Command.GET_TIME, callback, errCallback, []);
    }

    // set lobby_id to "" for the current one
    getSettings(lobby_id, callback, errCallback) {
        this._send(Command.GET_SETTINGS, callback, errCallback, [lobby_id]);
    }

    setUsername(username, callback, errCallback) {
        this._send(Command.SET_USERNAME, callback, errCallback, [username]);
    }

    setPlayerState(callback, errCallback) {
        this._send(Command.SET_PLAYER_STATE, callback, errCallback, []);
    }

    gotoPage(page_name, callback, errCallback) {
        this._send(Command.GOTO_PAGE, callback, errCallback, [page_name]);
    }

    getPage(page_name, callback, errCallback) {
        this._send(Command.GET_PAGE, callback, errCallback, [page_name]);
    }
    // DEPRECATED
    getPath(player_id, callback, errCallback) {
        this._send(Command.GET_PATH, callback, errCallback, [player_id]);
    }

    forceBeginGame(callback, errCallback) {
        this._send(Command.FORCE_BEGIN_GAME, callback, errCallback, []);
    }

    /**
     * default lobby SYSTEM commands
     */
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
     * Handlers for OUTGOING commands
     */
    registerAllLobbies(callback) {
        this._registerOutgoing(Command.ALL_LOBBIES, callback);
    }

    registerAllPlayers(callback) {
        this._registerOutgoing(Command.ALL_PLAYERS, callback);
    }

    registerBeginGame(callback) {
        this._registerOutgoing(Command.BEGIN_GAME, callback);
    }

    registerEndGame(callback) {
        this._registerOutgoing(Command.END_GAME, callback);
    }

    _registerOutgoing(command, callback) {
        this.pendingResponses[command.name] = {
            "command": command,
            "callback": callback,
            "errCallback" : () => {}, // no reason
            "timeout" : null          // no reason
        }
    }


    ready(callback) {
        this.readyCallbacks.push(callback);
    }

    /**
     * Primary WebSocket interpreters
     */
    ws_onopen() {
        // call ready callbacks
        for (let i = 0; i < this.readyCallbacks.length; i++) {
            this.readyCallbacks[i]();
        }

        // TODO: This
        // TODO: is just
        // TOOD: temporary
        // this.startLobby("HARD CODED CRAZY LIT LOBBY", () => console.log("Lobby started"), () => console.log("Lobby failed to start"));
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

                // if this is NOT a server command, just apply the callback on the payload too keep a nice interface
                if (actions.command.type !== COMMAND_TYPE.SERVER) {
                    actions.callback(parsedMsg.payload);
                } else {
                    actions.callback(parsedMsg);
                }
                window.clearTimeout(actions.timeout);
            } else {
                // (but give any command type access to the top-level error_message)
                const actions = this.pendingResponses[parsedMsg.command];
                if (actions.errCallback !== undefined) { actions.errCallback(parsedMsg); }
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
            "command": command,
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

/**
 * Create global server. NOTE THat it is done outside a doc.ready because it is needed everywhere at doc.ready().
 * And also does not need the DOM
 */
serverConn = new ServerConn(window.location.host + "/websocket");



/**
 * TESTING
 */
// serverConn.ready(() => {
//     serverConn.startLobby("CRAZY LIT TESTING LOBBY", () => {
//      serverConn.getPlayers("CRAZY LIT TESTING LOBBY",
//          (players) => {
//              console.log("SUCCESS: ");
//              console.log(players);
//          }, console.log);
//
//     }, console.log);

    //
    // getTime(callback, errCallback) {
    //     this._send(Command.GET_TIME, callback, errCallback, []);
    // }
    //
    // getSettings(lobby_id, callback, errCallback) {
    //     this._send(Command.GET_SETTINGS, callback, errCallback, [lobby_id]);
    // }
    //
    // gotoPage(page_name, callback, errCallback) {
    //     this._send(Command.GOTO_PAGE, callback, errCallback, [page_name]);
    // }
    //
    // getPage(page_name, callback, errCallback) {
    //     this._send(Command.GET_PAGE, callback, errCallback, [page_name]);
    // }
    //
    // getPath(player_id, callback, errCallback) {
    //     this._send(Command.GET_PATH, callback, errCallback, [player_id]);
    // }
    //
    // forceBeginGame(callback, errCallback) {
    //     this._send(Command.FORCE_BEGIN_GAME, callback, errCallback, []);
    // }
    //
    // // set lobby_id to null or undefined to generate a random one
    // startLobby(lobby_id, callback, errCallback) {
    //     this._send(Command.START_LOBBY, callback, errCallback, [lobby_id]);
    // }
    //
    // leaveLobby(callback, errCallback) {
    //     this._send(Command.LEAVE_LOBBY, callback, errCallback, []);
    // }
    //
    // joinLobby(lobby_id, callback, errCallback) {
    //     this._send(Command.JOIN_LOBBY, callback, errCallback, [lobby_id]);
    // }
    //
    // getLobbies(callback, errCallback) {
    //     this._send(Command.GET_LOBBIES, callback, errCallback, []);
    // }
// });

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

/**
 * A specific enum for game states.
 */
const GAME_STATE = {
  WAITING: 0,
  STARTED: 1,
  ENDED: 2
};

const ERROR_CODES = {
    TIMEOUT: 0,
    LOBBY_ID_TAKEN: 1, // TODO
    CANNOT_GOTO_PAGE: 2, // TODO
}

const GAME_MODES = {
    TIME_TRIAL: 0,
    LEAST_CLICKS: 1
}

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
      construct: (lobby_id, arguments) => {
          return { "lobby_id" : lobby_id, "arguments": arguments};
      }
    },
    LEAVE_LOBBY: {
      name: "leave_lobby",
      responseName: "leave_lobby_response",
      type: COMMAND_TYPE.SERVER,
      construct: () => { return {} }
    },
    JOIN_LOBBY: {
      name: "join_lobby",
      responseName: "join_lobby_response",
      type: COMMAND_TYPE.SERVER,
      construct: (lobby_id) => {
          return { "lobby_id" : lobby_id };
      }
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
        responseName: "all_players",
        type: COMMAND_TYPE.INCOMING,
        construct: (lobby_id) => {
            return { "lobby_id" : lobby_id };
        }
    },
    GET_TIME: {
        name: "get_time",
        responseName: "return_time",
        type: COMMAND_TYPE.INCOMING,
        construct: () => { return {} }
    },
    GET_SETTINGS : {
  		name: "get_settings",
          responseName: "return_settings",
  		type: COMMAND_TYPE.INCOMING,
  		construct: (lobby_id, state) => {
              return { "lobby_id" : lobby_id, "state": state};
          }
  	},
    FORCE_BEGIN_GAME: {
        name: "force_begin_game",
        responseName: "begin_game",
        type: COMMAND_TYPE.INCOMING,
        construct: () => { return {} }
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
  		construct: (page_name, initial) => {
              return {
                "page_name" : page_name,
                "initial" : initial
              };
          }
	  },
    GO_BACK_PAGE : {
  		name: "go_back_page",
      responseName: "return_goto_page",
  		type: COMMAND_TYPE.INCOMING,
  		construct: () => { return {} }
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
  		type: COMMAND_TYPE.OUTGOING,
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
        this.COMMAND_TIMEOUT = 10000; // some can take a while
        this.CLIENT_ID_COOKIE_EXPIRATION = 60;

        this.clientId = "";
        this.readyNow = false;
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

                // call ready callbacks added before we got here
                for (let i = 0; i < this.preReadyCallbacks.length; i++) {
                    this.preReadyCallbacks[i]();
                }

                // note we're ready so any further ready callbacks added are called immediately.
                this.readyNow = true;
            },
            "errCallback" : () => {}, // no reason
            "timeout" : null          // no reason
        }


        /**
         * callbacks called once websocket is ready
         */
        this.preReadyCallbacks = [];
    }

    _setId(id) {
        let d = new Date();
        d.setTime(d.getTime() + (this.CLIENT_ID_COOKIE_EXPIRATION * 60 * 1000)); //60 minutes
        let expires = "expires="+d.toUTCString();
        document.cookie = "client_id=" + id  + ":" + d.getTime() + ";" + expires;
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
    getSettings(lobby_id, state, callback, errCallback) {
        this._send(Command.GET_SETTINGS, callback, errCallback, [lobby_id, state]);
    }

    setUsername(username, callback, errCallback) {
        this._send(Command.SET_USERNAME, callback, errCallback, [username]);
    }

    setPlayerState(state, callback, errCallback) {
        this._send(Command.SET_PLAYER_STATE, callback, errCallback, [state]);
    }

    gotoPage(page_name, callback, errCallback) {
        this._send(Command.GOTO_PAGE, callback, errCallback, [page_name, false]);
    }

    goToInitialPage(callback, errCallback) {
        this._send(Command.GOTO_PAGE, callback, errCallback, ["", true]);
    }

    goBackPage(callback, errCallback) {
        this._send(Command.GO_BACK_PAGE, callback, errCallback);
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
    startLobby(lobby_id, args, callback, errCallback) {
        this._send(Command.START_LOBBY, callback, errCallback, [lobby_id, args]);
    }

    leaveLobby(callback, errCallback) {
        this._send(Command.LEAVE_LOBBY, callback, errCallback, []);
    }

    joinLobby(lobby_id, callback, errCallback) {
        this._send(Command.JOIN_LOBBY, callback, errCallback, [lobby_id]);
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

    registerError(callback) {
        this._registerOutgoing(Command.ERROR, callback);
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
        if (!this.readyNow) {
          // if we're not ready for registering things, defer until we are
          this.preReadyCallbacks.push(callback);
        } else {
          // otherwise, just call it now
          callback();
        }
    }

    /**
     * Primary WebSocket interpreters
     */
    ws_onopen() {
    };

    ws_onmessage(jsonMsg) {
        const parsedMsg = JSON.parse(jsonMsg.data);

        console.log("RECIEVING: ");
        console.log(parsedMsg);

        if (this.pendingResponses.hasOwnProperty(parsedMsg.command)) {
            if (parsedMsg.error_message === "") {
                // note that pendingResponses is a map from command RETURN MESSGAE NAME to the callbacks
                // that should be called on the return message, so we can reference these all in one line
                // without a switch statement
                const actions = this.pendingResponses[parsedMsg.command];
                window.clearTimeout(actions.timeout);

                // if this is NOT a server command, just apply the callback on the payload too keep a nice interface
                if (actions.callback !== undefined) {
                  if (actions.command.type !== COMMAND_TYPE.SERVER) {
                      actions.callback(parsedMsg.payload);
                  } else {
                      actions.callback(parsedMsg);
                  }
                }
            } else {
                // (but give any command type access to the top-level error_message)
                const actions = this.pendingResponses[parsedMsg.command];
                window.clearTimeout(actions.timeout);
                if (actions.errCallback !== undefined) { actions.errCallback(parsedMsg); }

                console.log("\nGot error: ");
                console.log(parsedMsg);
            }
        } else if (parsedMsg.error_message !== undefined && parsedMsg.error_message !== "") {
          const errCallback = this.pendingResponses[Command.ERROR.name].callback;
          if (errCallback !== undefined) { errCallback(parsedMsg); }
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
                    errCallback({
                        error_code: ERROR_CODES.TIMEOUT,
                        error_message: `Request for ${command.name} timed out`
                    });
                }
                console.log(`Request for ${command.name} timed out`);
            }, command === Command.START_LOBBY ? this.COMMAND_TIMEOUT * 3 : this.COMMAND_TIMEOUT)
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

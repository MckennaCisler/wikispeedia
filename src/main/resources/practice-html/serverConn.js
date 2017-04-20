// The actual getHtml will call the backend, but it'll also need to "hijack"
// the links to trigger a function that we've defined
// The code will look something like this:
// "<a href=\"javascript:linkClick()\">someLink</a>"
function getArticleHtmlTemp(articleTitle) {
  html = "";
  if (articleTitle == "Cat") {
    html = html + linkHelper("Dog", "Dogs") + " like to play "
            + linkHelper("Football", "football") + ".";
  } else if (articleTitle == "Dog") {
    html = html + longString + longString + "I don't like " + linkHelper("Cat", "cats") + "!";
  } else if (articleTitle == "Football") {
    html = html + linkHelper("Cat", "Cats") + " or " + linkHelper("Dog", "dogs")
            + " hmmmmmmmmmmmm.";
  }

  return html;
}

// Helper to get an "internal link"
function linkHelper(param, text) {
  return "<a href=\"javascript:linkClick('" + param + "')\">" + text + "</a>";
}




// Note that "INCOMING" and "OUTGOING" refer to the server perspective
const COMMAND_TYPE = {
  INCOMING: 0,
  RESPONSE: 1,
  OUTGOING: 2
};

const Command = {
    /**
     * Helpful regex:
     * ([A-Z_]+)\((\".*\")\, (COMMAND_TYPE\.\w+)\, \"(.*)\"\)(,//|;)
     * ->
     * $1 : { \n\t\tname: $2, \n\t\ttype: $3,\n\t\tconstruct: ($4) => {}\n\t},
     * (You'll need to do some manual chages... it's not the same on both sides)
     */
    /**
     * INCOMING Commands.
     */
    // Lobby-specific commands
    GET_PLAYERS : {
        name: "get_players",
        type: COMMAND_TYPE.INCOMING,
        construct: (lobby_id) => {
            return { "lobby_id" : lobby_id };
        }
    },
    GET_TIME: {name: "get_time",
        type: COMMAND_TYPE.INCOMING,
        construct: () => { return "" }
    },
    GET_SETTINGS : {
		name: "get_settings",
		type: COMMAND_TYPE.INCOMING,
		construct: (lobby_id) => {
            return { "lobby_id" : lobby_id };
        }
	},
    // Player-specific commands
    GOTO_PAGE : {
		name: "goto_page",
		type: COMMAND_TYPE.INCOMING,
		construct: (page_name) => {
            return { "page_name" : page_name };
        }
	},
    GET_PATH : {
		name: "get_path",
		type: COMMAND_TYPE.INCOMING,
        construct: (player_id) => {
            return {"player_id" : player_id}
        }
	},
    /**
     * RESPONSEs to INCOMING Commands.
     */
    // Lobby-specific commands
    RETURN_PLAYERS : {
		name: "return_players",
		type: COMMAND_TYPE.RESPONSE,
	},
    RETURN_TIME : {
		name: "return_time",
		type: COMMAND_TYPE.RESPONSE,
	},
    RETURN_SETTINGS : {
		name: "return_settings",
		type: COMMAND_TYPE.RESPONSE,
	},
    RETURN_PAGE : {
		name: "goto_page",
		type: COMMAND_TYPE.RESPONSE,
	},
    RETURN_PATH : {
		name: "return_path",
		type: COMMAND_TYPE.RESPONSE,
	},
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
        this.COMMAND_TIMEOUT = 2000;

        this.clientId = clientId;
        this.ws = new WebSocket("ws://" + source);
        this.ws.onopen = this.ws_onopen;
        this.ws.onmessage = this.ws_onmessage;
        this.ws.onclose = this.ws_onclose;

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
        this._send(Command.GET_PLAYERS, callback, errCallback, lobby_id);
    }

    getTime(callback, errCallback) {
        this._send(Command.GET_TIME, callback, errCallback);
    }

    getSettings(lobby_id, callback, errCallback) {
        this._send(Command.GET_SETTINGS, callback, errCallback, lobby_id);
    }

    gotoPage(page_name, callback, errCallback) {
        this._send(Command.GOTO_PAGE, callback, errCallback, page_name);
    }

    getPath(player_id, callback, errCallback) {
        this._send(Command.GET_PATH, callback, errCallback, player_id);
    }

    /**
     * Register a specific handler for the END_GAME message. Note that the handler is erased after every END_GAME call.
     */
    registerEndgame(callback) {
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
		// TODO
    };

    ws_onmessage(jsonMsg) {
        const parsedMsg = JSON.parse(jsonMsg);

        if (parsedMsg.client_id === this.clientId) { // TODO!!
            if (this.pendingResponses.contains(parsedMsg.command)) {
                // note we can't really localize an error command to a player
                if (parsedMsg.command === Command.ERROR.name) {
                    console.log("\nGot error: ");
                    console.log(parsedMsg);

                    // explicit loose typing to catch "", null
                } else if (parsedMsg.payload.error_message != undefined) {
                    const actions = this.pendingResponses[parsedMsg.command];
                    actions.errCallback(parsedMsg.payload);
                    window.clearTimeout(actions.timeout);
                } else {
                    const actions = this.pendingResponses[parsedMsg.command];
                    actions.callback(parsedMsg.payload);
                    window.clearTimeout(actions.timeout);
                }
            } else {
                console.log("\nUnknown command sent: ");
                console.log(parsedMsg);
            }
        }
    }

    ws_onclose() {
        // TODO
    }

    _send(command, callback, errCallback, args) {
        assert(command.type === COMMAND_TYPE.INCOMING);

        let msg = {
            "command": command.name,
            "payload": command.construct(args)
        };

        this.pendingResponses[command.name] = {
            "callback": callback,
            "errCallback": errCallback,
            "timeout": window.setTimeout(() => {
                errCallback("Request timed out");
            }, this.COMMAND_TIMEOUT)
        };

        this.ws.send(JSON.stringify(msg));
    }
}

$(document).ready(() => {
    // global
    var serverConn = new ServerConn("localhost:4568", "TODO - SOMETHING"); // TODO
})

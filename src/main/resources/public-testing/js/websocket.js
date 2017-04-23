let ws;
let $chatLog;

function setId(cvalue) {
    let d = new Date();
    d.setTime(d.getTime() + (15 * 60 * 1000)); //15 minutes
    let expires = "expires="+d.toUTCString();
    document.cookie = "client_id=" + cvalue + ";" + expires;
}

function getId() {
    const name = "client_id=";
    const ca = document.cookie.split(',');
    for(let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function send(obj) {
	ws.send(JSON.stringify(obj));
}

function setName(newName) {
	const payload = { "command":"set_client_id", "payload":{ "client_id":newName } };
  send(payload);
}

function joinLobby(lobbyName) {
	const payload = { "command":"join_lobby", "payload":{ "lobby_id":lobbyName } };
  send(payload);
}

function leaveLobby() {
	const payload = { "command":"leave_lobby" };
  send(payload);
}

function startLobby(lobbyName) {
	const payload = { "command":"start_lobby", "payload":{ "lobby_id":lobbyName } };
  send(payload);
}

function message(text) {
	const payload = { "command":"message", "payload":{ "message":text } };
  send(payload);
}

function whisper(recipient, text) {
	const payload = { "command":"whisper", "payload":{ "recipient":recipient, "message":text } };
  send(payload);
}

$(document).ready(() => {
  if ("WebSocket" in window)
  {
    console.log("WebSocket is supported by your Browser!");
		$chatLog = $("#chatlog");
    // Let us open a web socket
    ws = new WebSocket("ws://localhost:4567/websocket");

    ws.onopen = function()
    {
    };

    ws.onmessage = function (evt)
    {
      const received_msg = evt.data;

			const message = JSON.parse(received_msg)
      console.log(message);

			switch(message.command) {
				case "message":
					displayMessage("message", message.sender, message.message);
					break;
				case "whisper":
					displayMessage("whisper", message.sender, message.message);
					break;
				case "bounced_whisper":
					displayBouncedWhisper(message.target);
					break;
				case "set_id_response":
					setId(message.client_id);
					console.log(getId());
					break;
				default:
					console.log("unhandled message of type: " + message.type);
			}
    };

    ws.onclose = function()
    {
    };
  }

  else
  {
    // The browser doesn't support WebSocket
    alert("WebSocket NOT supported by your Browser!");
  }
});

function displayMessage(type, sender, message) {
	console.log("<p class='" + type + "'>" + sender + ": " + message + "</p>");
	$chatLog.prepend("<p class='" + type + "'>" + sender + ": " + message + "</p>");
}

function displayBouncedWhisper(recipient) {
	$chatLog.prepend("<p class='bounced'>" + recipient + " does not exist in this lobby.</p>");
}

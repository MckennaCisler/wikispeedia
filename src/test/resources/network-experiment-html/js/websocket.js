let ws;
let $chatLog;

function send(obj) {
	ws.send(JSON.stringify(obj));
}

function setName(newName) {
	const payload = { "command":"set_client_id", "client_id":newName };
  send(payload);
}

function joinLobby(lobbyName) {
	const payload = { "command":"join_lobby", "lobby_id":lobbyName };
  send(payload);
}

function leaveLobby() {
	const payload = { "command":"leave_lobby" };
  send(payload);
}

function startLobby(lobbyName) {
	const payload = { "command":"start_lobby", "lobby_id":lobbyName };
  send(payload);
}

function message(text) {
	const payload = { "command":"message", "message":text };
  send(payload);
}

function whisper(recipient, text) {
	const payload = { "command":"whisper", "recipient":recipient, "message":text };
  send(payload);
}

$(document).ready(() => {
  if ("WebSocket" in window)
  {
    console.log("WebSocket is supported by your Browser!");
		$chatLog = $("#chatlog");
    // Let us open a web socket
    ws = new WebSocket("ws://localhost:4568");

    ws.onopen = function()
    {
			setName("");
    };

    ws.onmessage = function (evt)
    {
      const received_msg = evt.data;

			const message = JSON.parse(received_msg)
      console.log(message);

			switch(message.type) {
				case "message":
					displayMessage("message", message.sender, message.message);
					break;
				case "whisper":
					displayMessage("whisper", message.sender, message.message);
					break;
				case "bounced_whisper":
					displayBouncedWhisper(message.target);
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

var ws;
var name = "anon";

function setName(newName) {
  ws.send("setname" + "newName")
  name = newName;
}

function joinLobby(text) {
  ws.send("join" + text);
}

function closeLobby() {
  ws.send("close lobby");
}

function startLobby(text) {
  ws.send("start" + text);
}

function message(text) {
  ws.send("message" + text);
}

$(document).ready(() => {
  if ("WebSocket" in window)
  {
    console.log("WebSocket is supported by your Browser!");

    // Let us open a web socket
    ws = new WebSocket("ws://localhost:4567");

    ws.onopen = function()
    {
    };

    ws.onmessage = function (evt)
    {
      var received_msg = evt.data;
      console.log(received_msg);
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

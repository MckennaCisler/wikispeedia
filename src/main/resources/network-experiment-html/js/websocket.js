var ws;
var name = "anon";

function setName(newName) {
  name = newName;
  ws.send(name + " has joined")
}

function message(text) {
  ws.send(name + ": " + text);
}

$(document).ready(() => {
  if ("WebSocket" in window)
  {
    console.log("WebSocket is supported by your Browser!");

    // Let us open a web socket
    ws = new WebSocket("ws://localhost:4567");

    ws.onopen = function()
    {
      ws.send("anon has joined")
    };

    ws.onmessage = function (evt)
    {
      var received_msg = evt.data;
      console.log(received_msg);
    };

    ws.onclose = function()
    {
      ws.send("a person has left")
    };
  }

  else
  {
    // The browser doesn't support WebSocket
    alert("WebSocket NOT supported by your Browser!");
  }
});

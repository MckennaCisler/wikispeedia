/* jshint esversion: 6 */

$(document).ready(() => {
	"use strict";
	if (!('import' in document.createElement('link'))) {
		$("#chat_button").hide();
		return;
	}
	let link = document.querySelector('link[rel="import"]');
    let content = link.import;
    // Grab DOM from chat.html's document.
    let el = content.querySelector('#chat_card');
    document.body.appendChild(el.cloneNode(true));

	$("#chat_card").hide();

	$("#chat_button").on('click', () => {
		$("#chat_card").toggle();
	});

	$("#xout").on('click', () => {
		$("#chat_card").hide();
	});

	$("#send_chat").on('click', () => {
		//TODO
		let text = $("#chat_text").val();
		serverConn.sendMessage(text);
		$("#chat_text").val("");
	});
});

serverConn.whenReadyToSend(() => {
	serverConn.getMessages(displayMessages, getMessagesError); });

$(document).keypress((e) => {
	"use strict";
	if (e.keyCode === 13) {
		if ($("#chat_text").is(':focus')) {
			$("#send_chat").click();
		}
	}
});

function getMessagesError(response) {
	console.log(response.error_message);
}

function displayMessages(messages) { //messages is a list of object of the form { timestamp: (milliseconds since epoch), message: (string content of the message), sender: (id of sender)}
	//console.log(messages);
	"use strict";
	$("#chat_button").fadeTo("fast", 0, () => {
		$("#chat_button").fadeTo("fast", 1.0);
	});
	if (messages.length === 0) {
		$("#chat_div").html("No one has said anything yet - start a conversation!");
	} else {
		$("#chat_div").html("");
		for (let i = 0; i < messages.length; i++) {
			let mess = messages[i];
			let mStr =
				`<div class="chat-bubble ${mess.sender_id === serverConn.clientId ? "chat-sender" : "chat-receiver"}">${mess.message}
					<br><div class="chat-metadata">Sent by: ${mess.sender} at ${new Date(mess.timestamp).toUTCString()}</div>
				</div>`;
			$("#chat_div").html($("#chat_div").html() + mStr);
		}
		$(".chat-bubble").get($(".chat-bubble").length - 1).scrollIntoView();
	}
}

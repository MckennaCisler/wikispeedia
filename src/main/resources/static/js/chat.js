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

function displayMessages(messages) { //messages is a list of object of the form { timestamp: (milliseconds since epoch), message: (string content of the message)}
	console.log(messages);
}

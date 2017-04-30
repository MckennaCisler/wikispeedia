/* jshint esversion: 6 */

$(document).ready(() => {
	"use strict";
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
		alert(`gonna send ${text} eventually`);
		$("#chat_text").val("");
	});
});

$(document).keypress((e) => {
	"use strict";
	if (e.keyCode === 13) {
		if ($("#chat_text").is(':focus')) {
			$("#send_chat").click();
		}
	}
});
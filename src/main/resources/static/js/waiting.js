/*jshint esversion: 6 */

// Globals


//Maybe do this with the title instead
let dots = 0;
let ddd = window.setInterval(() => {
	"use strict";
	dots++;
	if (dots === 4) {
		dots = 0;
	}
	let dot_str = "";
	for (let i = 0; i < dots; i++) {
		dot_str += ".";
	}
	document.title = "Waiting" + dot_str;
}, 1000);

$(document).ready(() => {
	"use strict";
	let keepGoing = true;

	$("#force").on('click', () => {
		$(".loader").hide();
		$("#counter").show();
		startGame();
	});
});

function decrement(pid) {
	if ($("#counter").html() === "0") {
		clearInterval(pid);
		window.location.replace("end");
	} else {
		$("#counter").html($("#counter").html() - 1);
		if ($("#counter").html() > 1) {
			let audio = new Audio('lib/assets/beep.wav');
			audio.play();
		} else if (keepGoing) {
			let audio = new Audio('lib/assets/launch.wav');
			audio.play();
			keepGoing = false;
		}
	}
};

function startGame() {
	clearInterval(ddd);
	document.title = "The game is starting";
	let audio = new Audio('lib/assets/beep.wav');
	audio.play();
	var pid = setInterval(decrement, 1000, pid);
}

// game logic handlers
serverConn.ready(() => {
	// Get player states
	serverConn.registerAllPlayers(drawPlayers);
	serverConn.registerBeginGame(startGame); // TODO: Game will be 5s off in time!!!!

	// Get current lobby settings
	serverConn.getSettings("", GameState.WAITING, (settings) => {
 		// Get articles
		serverConn.getPage(settings.start.name, drawFirstPage, displayServerConnError);
		serverConn.getPage(settings.goal.name, drawSecondPage, displayServerConnError);
	}, displayServerConnError);

});

function drawFirstPage(article) {
	// TODO
	console.log(article);
}

function drawSecondPage(article) {
	// TODO
	console.log(article);
}

function drawPlayers(players) {
	for (let i = 0; i < players.length; i++) {
		console.log(player[i]);
	}
}

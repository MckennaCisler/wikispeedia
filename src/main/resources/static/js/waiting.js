/*jshint esversion: 6 */

// Globals
let fakeId = "c";
let $players;
let $title1;
let $blurb1;
let $title2;
let $blurb2;

// Maybe do this with the title instead
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
	$players = $("#players");
	$title1 = $("#a1_title");
	$blurb1 = $("#a1_blurb");
	$title2 = $("#a2_title");
	$blurb2 = $("#a2_blurb");

	drawPlayersFake(playersFake);
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
		serverConn.getPage(settings.start.name, (article) => {
			drawFirstPage(article);
			serverConn.getPage(settings.goal.name, drawSecondPage, displayServerConnError);
		}, displayServerConnError);
	}, displayServerConnError);

});

function drawFirstPage(article) {
	$title1.text(article.title);
	// $blurb1.text(firstSentence(article.blurb));
}

function drawSecondPage(article) {
	$title2.text(article.title);
	// $blurb2.text(firstSentence(article.blurb));
}

function drawPlayers(players) {
	// TODO: Replace with drawPlayersFake

	for (let i = 0; i < players.length; i++) {
		console.log(player[i]);
	}
}

let playersFake = [{name : "Rohan", id : "a"}, {name : "McKenna", id : "b"}, {name : "Jacob", id : "c"}, {name : "Sean", id : "d"}];
function drawPlayersFake(players) {
	// example line: <li class="list-group-item"><input type="checkbox" id="u0" checked disabled> You</li>
	for (let i = 0; i < players.length; i++) {
		if (players[i].id == fakeId) {
			$("<li class=\"list-group-item\"><input type=\"checkbox\">&nbsp<b>Me</b></li>")
				.appendTo($players)
				.click(function() {
					console.log(players[i].id, $(this).children()[0].checked);
				});
		} else {
			$("<li class=\"list-group-item\"><input type=\"checkbox\" disabled>&nbsp" + players[i].name + "</li>")
				.appendTo($players);
		}
	}
}

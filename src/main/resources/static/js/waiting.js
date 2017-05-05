/*jshint esversion: 6 */

// Globals
let fakeId = "c";
let players;
let $title1;
let $blurb1;
let $title2;
let $blurb2;
let keepGoing = true;
let loader = true;

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

$(window).resize(resize);

function resize() {
	"use strict";
	if ($(window).width() <= 700) {
		$('.loader').hide();
		$("#waiting_card").removeClass("row");
		$("#players").removeClass("col-4");
	} else if (loader) {
		$('.loader').show();
		$("#waiting_card").addClass("row");
		$("#players").addClass("col-4");
	}

	if (!loader && $(window).width() > 700) {
		$("#counter").show();
	}
}

function decrement(pid) {
	"use strict";
	if ($("#counter").html() === "0") {
		clearInterval(pid);
		window.location.replace("play");
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
}

function startGame() {
	"use strict";
	clearInterval(ddd);
	document.title = "The game is starting";
	let audio = new Audio('lib/assets/beep.wav');
	audio.play();
	$(".loader").hide();
	if ($(window).width() > 700) {
		$("#counter").show();
	}
	loader = false;
	var pid = setInterval(decrement, 1000, pid);
}


$(document).ready(() => {
	"use strict";
	resize();
	$('[data-toggle="tooltip"]').tooltip();
	keepGoing = true;

	let $players = $("#players");
	let $title1 = $("#a1_title");
	let $blurb1 = $("#a1_blurb");
	let $title2 = $("#a2_title");
	let $blurb2 = $("#a2_blurb");

	$title1.html("Loading...");
	$title2.html("Loading...");

	$("#force").on('click', () => {
		startGame();
	});

	$("#leave").on('click', () => {
		serverConn.leaveLobby(() => {
			window.location.href = "/";
		}, displayServerConnError)
	});

	// game logic handlers
	serverConn.whenReadyToRecieve(() => {
		"use strict";
		serverConn.registerError(displayServerConnError);
		serverConn.registerBeginGame(startGame);
	});

	serverConn.whenReadyToSend(() => {
		// wait until we have client id to register this one
		serverConn.registerAllPlayers(drawPlayers);

		// Get player states
		serverConn.getPlayers("", drawPlayers, displayServerConnError); // get the players in THIS lobby

		// Get current lobby settings
		serverConn.getSettings("", GAME_STATE.WAITING, (settings) => {
	 		// Get articles
			serverConn.getPage(settings.startPage.name, (article) => {
				drawFirstPage(article);
				serverConn.getPage(settings.goalPage.name, drawSecondPage, displayServerConnError);
			}, displayServerConnError);
		}, displayServerConnError);
	});

	function drawFirstPage(article) {
		"use strict";
		$title1.html("<a href=\"" + article.href + "\" target=\"_blank\">" + article.title + "</a>");
		// $blurb1.text(firstSentence(article.blurb));
	}

	function drawSecondPage(article) {
		"use strict";
		$title2.html("<a href=\"" + article.href + "\" target=\"_blank\">" + article.title + "</a>");
		// $blurb2.text(firstSentence(article.blurb));
	}

	//let playersFake = [{name : "Rohan", id : "a"}, {name : "McKenna", id : "b"}, {name : "Jacob", id : "c"}, {name : "Sean", id : "d"}];
	function drawPlayers(players) {
		"use strict";
		// example line: <li class="list-group-item"><input type="checkbox" id="u0" checked disabled> You</li>
		$players.html("");
		for (let i = 0; i < players.length; i++) {
			if (players[i].id === serverConn.clientId) {
				if (!players[i].ready) {
					$("<li class=\"list-group-item\"><div class=\"me_li\"><div style=\"align-self: flex-start;\"><b>Me</b></div><button class=\"btn btn-outline-success\" id=\"my_but\" style=\"align-self: flex-end;\">Click when ready</button></li>")
					.appendTo($players);
					$("#my_but").on('click', function() {
						serverConn.setPlayerState(true);
					});
				} else {
					$("<li class=\"list-group-item\"><div class=\"me_li\"><div style=\"align-self: flex-start;\"><b>Me</b></div><button class=\"btn btn-success\" id=\"my_but\" style=\"align-self: flex-end;\" disabled>Click when ready</button></li>")
					.appendTo($players);
				}
			} else {
				$("<li class=\"list-group-item\"><input type=\"checkbox\" disabled>&nbsp" + players[i].name + "</li>")
					.appendTo($players);
			}
		}
	}
});

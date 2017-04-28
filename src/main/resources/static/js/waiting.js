/*jshint esversion: 6 */

// Globals
let fakeId = "c";
let $players;
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

$(document).ready(() => {
	"use strict";
	resize();
	$('[data-toggle="tooltip"]').tooltip();
	keepGoing = true;

	$("#force").on('click', () => {
		startGame();
	});
});

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

// game logic handlers
serverConn.ready(() => {
	"use strict";
	$players = $("#players");
	$title1 = $("#a1_title");
	$blurb1 = $("#a1_blurb");
	$title2 = $("#a2_title");
	$blurb2 = $("#a2_blurb");

	// Get player states
	serverConn.registerAllPlayers(drawPlayers);
	serverConn.getPlayers(drawPlayers);
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
	"use strict";
	$title1.html("<a href=\"" + article.href + "\" target=\"_blank\">" + article.title + "</a>");
	// $blurb1.text(firstSentence(article.blurb));
}

function drawSecondPage(article) {
	"use strict";
	$title2.html("<a href=\"" + article.href + "\" target=\"_blank\">" + article.title + "</a>");
	// $blurb2.text(firstSentence(article.blurb));
}

function dimMyBut() {
	"use strict";
	$("#my_but").removeClass("btn-outline-success");
	$("#my_but").addClass("btn-success");
	$("#my_but").attr('disabled', true);
}

let playersFake = [{name : "Rohan", id : "a"}, {name : "McKenna", id : "b"}, {name : "Jacob", id : "c"}, {name : "Sean", id : "d"}];
function drawPlayers(players) {
	"use strict";
	// example line: <li class="list-group-item"><input type="checkbox" id="u0" checked disabled> You</li>
	$players.html("");
	for (let i = 0; i < players.length; i++) {
		if (players[i].id === serverConn.clientId) {
			if (!players[i].ready) {
				$("<li class=\"list-group-item\"><div><p style=\"float: left;\"><b>Me</b></p><button class=\"btn btn-outline-success\" id=\"my_but\" style=\"float: right;\">Click when ready</button></li>")
				.appendTo($players);
				$("#my_but").on('click', function() {
					serverConn.setPlayerState(true);
				});
			} else 
				$("<li class=\"list-group-item\"><b>Me</b><button class=\"btn btn-success\" id=\"my_but\" style=\"float: right;\" disabled>Click when ready</button></li>")
				.appendTo($players);
		} else {
			$("<li class=\"list-group-item\"><input type=\"checkbox\" disabled>&nbsp" + players[i].name + "</li>")
				.appendTo($players);
		}
	}
}

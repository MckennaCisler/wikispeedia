/*jshint esversion: 6 */

// globals for use in game logic
let isMaking;
let lobbyName;

$(document).ready(function () {
	"use strict";
	resize();

	$("#advanced_ops").hide();
	$("#advanced").on('click', () => {
		if ($("#advanced_ops").is(":hidden")) {
			$("#advanced_ops").show();
			$("#advanced_im").attr("src", "lib/assets/down-triangle.png");
		} else {
			$("#advanced_ops").hide();
			$("#advanced_im").attr("src", "lib/assets/side-triangle.png");
		}
	});

	$("#start_game").on('click', () => {
		if ($("#game_name").val() === "") {
			$("#game_name").effect("highlight", {
				"color": "red"
			}, 1000);
		} else {
			isMaking = true;
			lobbyName = $("#game_name").val();
			$("#main").hide();
			$("#rules").hide();
			$("#uname_picker").show();
			$("#u_header").html(lobbyName);
			console.log(lobbyName);
		}
	});

	$("#open_games").on('click', (event) => {
		isMaking = false;
		lobbyName = $(event.target).html();
		$("#main").hide();
		$("#rules").hide();
		$("#uname_picker").show();
		$("#u_header").html(lobbyName);
	});

	$("#back_main").on('click', () => {
		$("#uname_picker").hide();
		$("#main").show();
		$("#rules").show();
	});


});

$(window).resize(function () {
	"use strict";
	resize();
});

function resize() {
	"use strict";
	if ($(window).width() <= 600) {
		$('#main_buttons').removeClass("btn-group");
	} else {
		$('#main_buttons').addClass("btn-group");
	}

	if ($(window).width() <= 800) {
		$('#row_div').removeClass("row");
	} else {
		$('#row_div').addClass("row");
	}
}

// game logic handlers
serverConn.ready(() => {
	// setup lobbies
	serverConn.registerAllLobbies(drawLobbies);

	$("#launch").on('click', () => {
		if ($("#uname").val() === "") {
			$("#uname").effect("highlight", {
				"color": "red"
			}, 1000);
		} else if (lobbyName !== "") {
			if (isMaking) {
				serverConn.startLobby(lobbyName.trim(),
				() => { window.location.href = "waiting"; },
				(error) => { displayError(error.error_message); });
			} else {
				serverConn.joinLobby(lobbyName.trim(),
				() => { window.location.href = "waiting"; },
				(error) => { displayError(error.error_message); });
			}
		}
	});
});

function drawLobbies(lobbies) {
	for (let i = 0; i < lobbies.length; i++) {
		$("#open_games").append('<li class="alobby list-group-item list-group-item-action" id="' +
		 	lobbies[i].id + '">' + lobbies[i].id + '</li>');
	}
}

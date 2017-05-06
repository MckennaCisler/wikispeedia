/*jshint esversion: 6 */

// globals for use in game logic
let isMaking;
let lobbyName;

$(document).ready(function () {
	"use strict";
	resize();
	$('[data-toggle="tooltip"]').tooltip();
	setCookie('timePlayed', '');
	setCookie('songChoice', '');
	//document.cookie = 'timePlayed=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
	$("#advanced_ops").hide();
	$("#difficulty_slider").slider();
	$("#advanced").on('click', () => {
		if ($("#advanced_ops").is(":hidden")) {
			$("#advanced_ops").show();
			$("#advanced_im").attr("src", "lib/assets/down-triangle.png");
		} else {
			$("#advanced_ops").hide();
			$("#advanced_im").attr("src", "lib/assets/side-triangle.png");
		}
	});

	$("#open_games").on('click', (event) => {
		isMaking = false;
		const $target = $(event.target);
		if (!$("#open_games").hasClass("none-found")) {
			lobbyName = $target.html();
			$("#main").hide();
			$("#rules").hide();
			$("#uname_picker").show();
			$("#u_header").html(lobbyName);
		}
	});

	$("#back_main").on('click', () => {
		serverConn.leaveLobby(() => {
			$("#start_game").prop('disabled', false);
			$("#start_game").html(`Start your game!`);
			$("#uname_picker").hide();
			$("#main").show();
			$("#rules").show();
			$(`#${lobbyName}`).remove();
		}, displayServerConnError);
	});
	
	$("#music-select").change(() => {
		let mval = document.getElementById("music-select").options.selectedIndex;
		setCookie('songChoice', mval);
	});

	$("#start_game").on('click', () => {
		if ($("#game_name").val() === "") {
			$("#game_name").effect("highlight", {
				"color": "red"
			}, 1000);
		} else {
			isMaking = true;
			lobbyName = $("#game_name").val();

			let mode;
			if (document.getElementById("time").checked) {
				mode = GAME_MODES.TIME_TRIAL;
			} else {
				mode = GAME_MODES.LEAST_CLICKS;
			}

			//make loader show up here
			$("#start_game").prop('disabled', true);
			$("#start_game").html(`<div class="loader2"></div>`);

			serverConn.startLobby(lobbyName.trim(),
				{
					"gameMode": mode,
					"difficulty": $("#difficulty_slider").val() / 100
				},
				() => {
					$("#main").hide();
					$("#rules").hide();
					$("#uname_picker").show();
					$("#u_header").html(lobbyName);
				},
				(e) => {
					$("#start_game").prop('disabled', false);
					$("#start_game").html(`Start your game!`);
					displayServerConnError(e);
				});
			console.log(lobbyName);
		}
	});

	$("#launch").on('click', () => {
		if ($("#uname").val() === "") {
			$("#uname").effect("highlight", {
				"color": "red"
			}, 1000);

		} else if (lobbyName !== "") {
			// TODO: make the audio cookie
			if (isMaking) {
				// only have to do this, lobby was created & joined earlier
				serverConn.setUsername($("#uname").val(), () => {
					setCookie('timePlayed', audio.currentTime);
					window.location.href = "waiting";
				}, displayServerConnError);

			} else {
				serverConn.joinLobby(lobbyName.trim(),
				() => {
					serverConn.setUsername($("#uname").val(), () => {
						setCookie('timePlayed', audio.currentTime);
						window.location.href = "waiting";
					}, displayServerConnError);
				},
				displayServerConnError);
			}
		}
	});
});

$(document).keypress((e) => {
	"use strict";
	if (e.keyCode === 13) {
		if ($("#main").is(":hidden") && !$("#chat_text").is(':focus')) {
			$("#launch").click();
		} else if (!$("#chat_text").is(':focus')) {
			$("#start_game").click();
		}
	}
});

$(window).resize(resize);

function resize() {
	"use strict";
	if ($(window).width() <= 400) {
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

/*function startLobby(callback, errCallback) {
	serverConn.startLobby(lobbyName.trim(),
		() => {
			callback();
		},
		(error) => {
			errCallback(error.error_message);
			displayServerConnError(error);
		});
}*/

// game logic handlers
serverConn.whenReadyToRecieve(() => {
	"use strict";
	serverConn.registerAllLobbies(drawLobbies);
	serverConn.registerError(displayServerConnError);
});

serverConn.whenReadyToSend(() => {
	"use strict";
	serverConn.leaveLobby();
});

function drawLobbies(lobbies) {
	"use strict";
	$("#open_games").html("");
	if (lobbies.length === 0) {
		$(document).ready(() => {
			$("#open_games").html("No games were found, you'll have to make your own!");
			$("#open_games").addClass("none-found");
		});
	} else {
		$(document).ready(() => {
			$("#open_games").removeClass("none-found");
			for (let i = 0; i < lobbies.length; i++) {
				$("#open_games").append('<li class="alobby list-group-item list-group-item-action" id="' +
					lobbies[i].id + '">' + lobbies[i].id + '</li>');
			}
		});
	}
}

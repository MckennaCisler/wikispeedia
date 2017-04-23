/*jshint esversion: 6 */
$(document).ready(function () {
	"use strict";
	resize();
	let isMaking;
	let lobbyName;

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

	$("#launch").on('click', () => {
		if ($("#uname").val() === "") {
			$("#uname").effect("highlight", {
				"color": "red"
			}, 1000);
		} else {
			// BE SURE TO ACTUALLY DO SOMETHING WITH THE ID
			window.location.href = "waiting.html";
		}
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
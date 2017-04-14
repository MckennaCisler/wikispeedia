$(document).ready(function() {
	"use strict";
	let isMaking;
	let lobbyName;
	
	$("#advanced_ops").hide();
	$("#advanced").on('click', () => {
		if ($("#advanced_ops").is(":hidden")) {
			$("#advanced_ops").show();
			$("#advanced_im").attr("src", "down-triangle.png");
		} else {
			$("#advanced_ops").hide();
			$("#advanced_im").attr("src", "side-triangle.png");
		}
	});
	
	$("#start_game").on('click', () => {
		if ($("#game_name").val() === "") {
			$("#game_name").effect("highlight", {"color": "red"}, 1000);
		} else {
			isMaking = true;
			lobbyName = $("#game_name").val();
			$("#main").hide();
			$("#uname_picker").show();
			$("#u_header").html(lobbyName);
			console.log(lobbyName);
		}
	});
	
	$("#open_games").on('click', (event) => {
		isMaking = false;
		lobbyName = $(event.target).html();
		$("#main").hide();
		$("#uname_picker").show();
		$("#u_header").html(lobbyName);
	});
	
	$("#back_main").on('click', () => {
		$("#uname_picker").hide();
		$("#main").show();
	});
	
	$("#launch").on('click', () => {
		if ($("#uname").val() === "") {
			$("#uname").effect("highlight", {"color": "red"}, 1000);
		} else {
			// BE SURE TO ACTUALLY DO SOMETHING WITH THE ID
			window.location.href = "waiting.html";
		}
	});
});
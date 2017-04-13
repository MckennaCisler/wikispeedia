$(document).ready(function() {
	let isMaking;
	"use strict";
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
			$("#game_name").animate({color:'red'}, 400).fadeIn(200).fadeOut(200).fadeIn(200).fadeOut(200).fadeIn(200);
			$("#game_name").animate({color:'white'}, 100);
			return;
		}
		isMaking = true;
		$("#main").hide();
		$("#uname_picker").show();
	});
	
	$("#open_games").on('click', () => {
		isMaking = false;
		$("#main").hide();
		$("#uname_picker").show();
	});
	
	$("#back_main").on('click', () => {
		$("#uname_picker").hide();
		$("#main").show();
	});
});
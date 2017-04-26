/*jshint esversion: 6 */
let winner = true;
let time = 312;
let increment = () => {
	"use strict";
	time++;
	let minutes = Math.floor(time / 60);
	var seconds = time - minutes * 60;
	$("#end_timer").html(minutes + ":" + seconds);
	$(".going").html(time + " seconds...");
};

$(document).ready(() => {
	"use strict";

	let canvases = document.getElementsByClassName("end_canvas");
	for (let i = 0; i < canvases.length; i++) {
		let ctx = canvases[i].getContext("2d");
		ctx.fillStyle = "#003366";
		ctx.fillRect(0, 0, canvases[i].width, canvases[i].height);
	}

	if (winner) {
		let audio = new Audio("lib/assets/applause.wav");
		audio.play();
	}

	setInterval(increment, 1000);
});


serverConn.ready(() => {
	"use stirct";
	// get the info and display it

	serverConn.registerAllPlayers("", (players) => {
		// update player info as they move through pages
		$("#players").html("");
		for (let i = 0; i < players.length; i++) {
			let listr = '<li class="list-group-item end_row';
			//ok so sorry I didn't have time to finish this but I don't know the commands and I'm leaving so hopefully this gives you enough direction to start
			$("#players").append(listr);
		}
	}, (e) => {alert(e);});
});

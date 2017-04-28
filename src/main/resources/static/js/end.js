/*jshint esversion: 6 */
let winner = true;
let time = 312;
let increment = () => {
	"use strict";
	time++;
	let minutes = Math.floor(time / 60);
	var seconds = time - minutes * 60;
	$("#end_timer").html(minutes + ":" + (seconds < 10 ? "0" + seconds : seconds));
	$(".going").html(time + " seconds...");
};

$(document).ready(() => {
	"use strict";

	//let canvases = document.getElementsByClassName("end_canvas");
	//for (let i = 0; i < canvases.length; i++) {
	//	let ctx = canvases[i].getContext("2d");
	//	ctx.fillStyle = "#003366";
	//	ctx.fillRect(0, 0, canvases[i].width, canvases[i].height);
	//}

	resize();
	if (winner) {
		let audio = new Audio("lib/assets/applause.wav");
		audio.play();
	}

	setInterval(increment, 1000);
});

$(window).resize(resize);

function resize() {
	"use strict";
	if ($(window).width() <= 700) {
		$('#end_timer').hide();
	} else {
		$('#end_timer').show();
	}
}

serverConn.ready(() => {
	"use stirct";
	// get the info and display it

	serverConn.registerAllPlayers("", (players) => {
		// update player info as they move through pages
		let $players = $("#players");
		$players.html("");
		for (let i = 0; i < players.length; i++) {
			//example line: <li class="list-group-item end_row"><strong class="end_row_name">Sean: </strong><strong class="end_row_time">273 seconds</strong></li>
			$players.html("");
			for (let i = 0; i < players.length; i++) {
				if (players[i].id === serverConn.clientId) {
					//$players.append(`<li class="list-group-item end_row ${catchme}"><strong class="end_row_name">${players[i].name}</strong><strong class="end_row_time ${catchme}">${time} seconds${catchme}</strong></li>`);
				} else {
					//$players.append("hi");
				}
				$players.append(`<li class="list-group-item end_row"><strong class="end_row_name">${players[i].name}</strong><strong class="end_row_time">Not working yet seconds</strong></li>`);
			}
		}
	}, (e) => {
		alert(e);
	});
});

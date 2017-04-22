/*jshint esversion: 6 */

//Maybe do this with the title instead
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
	let keepGoing = true;
	const decrement = (pid) => {
		if ($("#counter").html() === "0") {
			clearInterval(pid);
			window.location.replace("end.html");
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
	$("#force").on('click', () => {
		$(".loader").hide();
		$("#counter").show();
		clearInterval(ddd);
		document.title = "The game is starting";
		let audio = new Audio('lib/assets/beep.wav');
		audio.play();
		var pid = setInterval(decrement, 1000, pid);
	});
});
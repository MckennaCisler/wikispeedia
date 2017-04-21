/*jshint esversion: 6 */

window.setInterval(() => {
	"use strict";
	const dot_str = $("#dots").html();
	if (dot_str.length === 2) {
		$("#dots").html("...");
	} else if (dot_str.length === 1) {
		$("#dots").html("..");
	} else if (dot_str.length === 3) {
		$("#dots").html("");
	} else {
		$("#dots").html(".");
	}
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
				let audio = new Audio('assets/beep.wav');
				audio.play();
			} else if (keepGoing) {
				let audio = new Audio('assets/launch.wav');
				audio.play();
				keepGoing = false;
			}
		}
	};
	$("#force").on('click', () => {
		$("#counter").show();
		let audio = new Audio('assets/beep.wav');
		audio.play();
		var pid = setInterval(decrement, 1000, pid);
	});
});
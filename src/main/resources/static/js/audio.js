/* jshint esversion: 6 */
let audio = new Audio();

function setCookie(c_name, value) {
	"use strict";
	document.cookie = c_name + "=" + value;
}

function getCookie(c_name) {
	"use strict";
	let i, x, y, ARRcookies = document.cookie.split(";");
	for (i = 0; i < ARRcookies.length; i++) {
		x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("="));
		y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1);
		x = x.replace(/^\s+|\s+$/g, "");
		if (x === c_name) {
			return y;
		}
	}
}

$(document).ready(() => {
	"use strict";
	let song = getCookie('songChoice');
	if (song == 1) {
		audio = new Audio("lib/assets/Shiny-Spaceship.mp3");
	} else if (song == 2) {
		audio = new Audio("lib/assets/space-gone.mp3");
	} else if (song == 3) {
		audio = new Audio("lib/assets/All-Star.mp3");
	}
	
	let tillPlayed = getCookie('timePlayed');
	if (tillPlayed) {
		audio.currentTime = tillPlayed;
	}
	
	setInterval(update, 1000);
});

function update() {
	"use strict";
	let tillPlayed = getCookie('timePlayed');
	let song = getCookie('songChoice');
	if (song == 0) {
		audio.pause();
	} else if (song == 1) {
		if (!audio.currentSrc.includes("Shiny-Spaceship.mp3")) {
			audio.pause();
			audio = new Audio("lib/assets/Shiny-Spaceship.mp3");
			audio.loop = true;
		}
		audio.play();
	} else if (song == 2) {
		if (!audio.currentSrc.includes("space-gone.mp3")) {
			audio.pause();
			audio = new Audio("lib/assets/space-gone.mp3");
			audio.loop = true;
		}
		audio.play();
	} else if (song == 3) {
		if (!audio.currentSrc.includes("All-Star.mp3")) {
			audio.pause();
			audio = new Audio("lib/assets/All-Star.mp3");
			audio.loop = true;
		}
		audio.play();
	}
}
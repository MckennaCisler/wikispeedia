/* jshint esversion: 6 */

$(document).ready(() => {
	resize();
});

$(window).resize(resize);

function resize() {
	"use strict";
	
	if ($(window).width() < 400) {
		$(".title-a").html("WS");
	} else {
		$(".title-a").html("Wikispeedia");
	}
}
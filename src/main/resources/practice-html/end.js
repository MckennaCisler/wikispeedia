/*jshint esversion: 6 */

$(document).ready(() => {
	"use strict";
	
	let canvases = document.getElementsByClassName("end_canvas");
	for (let i = 0; i < canvases.length; i++) {
		let ctx = canvases[i].getContext("2d");
		ctx.fillStyle = "#003366";
		ctx.fillRect(0, 0, canvases[i].width, canvases[i].height);
	}
});
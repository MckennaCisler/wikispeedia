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
	const decrement = (pid) => {
		if ($("#counter").html() === "0") {
			clearInterval(pid);
			alert("Dummy. Will move on to game in the future");
		} else {
			$("#counter").html($("#counter").html() - 1);
		}
	};
	$("#force").on('click', () => {
		$("#counter").show();
		var pid = setInterval(decrement, 1000, pid);
	});
});
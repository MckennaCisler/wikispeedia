let currSeconds = 0;
let setWinner = false;

let docReady = false;
let serverReady = false;

let leaderboard = true;
let recentPlayers = [];

let endPage = "";
let shortestPath = true;

$(document).ready(() => {
	resize();
	if (serverReady) {
		console.log("REGISTERED");
		serverConn.registerAllPlayers(playersCallback);
	}

	$("#disp-buttons").on("change", (event) => {
		leaderboard = event.target.id == "lead";
		console.log(recentPlayers);
		playersCallback(recentPlayers);
	});

	$(window).resize(() => {
		if (!leaderboard) {
			playersCallback(recentPlayers);
		}
	});

	docReady = true;
});

$(window).resize(resize);

function resize() {
	"use strict";
	if ($(window).width() <= 650) {
		$("#disp-buttons").hide();
	} else {
		$("#disp-buttons").show();
	}
}

serverConn.whenReadyToRecieve(() => {
	serverConn.registerError(displayServerConnErrorRedirectHome);
	serverConn.registerClose(displayConnCloseMsg);

	// TODO: Get settings: game type and end page

	if (docReady) {
		console.log("REGISTERED");
		serverConn.registerAllPlayers(playersCallback);
	} else {
		serverReady = true;
	}
});

serverConn.whenReadyToSend(() => {
	serverConn.getPlayers("", playersCallback, displayServerConnError);
	serverConn.getSettings("", GAME_STATE.ENDED, settingsCallback, settingsError);
});

function settingsCallback(settings) {
	shortestPath = settings.gameMode == 1;
	console.log("SETTTINGS");
	console.log(settings);
	endPage = settings.goalPage.name;
}

function settingsError(error) {
	// TODO
}

// Updates the player to time map and redraws the results
function playersCallback(players) {
	recentPlayers = players;
	if (leaderboard) {
		drawResults(players);
	} else {
		drawHistory(players);
	}
}

// Draws the results
function drawResults(players) {
	$("svg").html("");
	$("#legend").html("");
	d3.select("#svg").attr("height", "0px");
	playersSorted = players.sort( function(a, b) {return a.path.length - b.path.length} );
	winners = [];

	for (let i = 0; i < playersSorted.length; i++) {
		let player = playersSorted[i];

		nameHtml = player.name;
		if (player.id == serverConn.clientId) {
			nameHtml = "<b>" + nameHtml + "</b>";
		}

		let style = "";
		if (i == 0) {
			$("#leaderboard").html("<thead><tr><th>Name</th><th># of clicks</th></tr></thead>");
		}

		if (player.isWinner) {
			style = "table-success";
			winners.push(player);
		} else if (!player.done) {
			style = "table-danger";
		}

		$el = $(`<tr><td>${nameHtml}</td><td>${player.path.length}</td></tr>`)
						.appendTo($("#leaderboard"))
						.attr("class", "" + style);
	}

	winnerStr = "";
	if (winners.length == 0) {
		winnerStr = "<b>No winners</b>";
	} else if (winners.length == 1) {
		winnerStr = `<b>Winner: ${winners[0].name}</b>`;
	} else if (winners.length == 2) {
		winnerStr = `<b>Winners: ${winners[0].name} and ${winners[1].name}`;
	} else {
		winnerStr = "<b>Winners: ";
		for (let i = 0; i < winners.length; i++) {

			if (i < winners.length - 1) {
				winnerStr = winnerStr + winners[i].name + ", ";
			} else {
				winnerStr = winnerStr + "and " + winners[i].name;
			}
		}

		winnerStr = winnerStr + "</b>"
	}

	$("#winner").html(winnerStr);

	$("#stats").html("");
	// TODO: Only if it's time trial
	if (winners.length > 0 && !shortestPath) {
		$("#stats").html(`<br><i>Winning time: ${millisecondsToStr(winners[0].playTime)}</i>`);
	} else {
		$("#stats").html("");
	}
}

let inContainer;
function drawHistory(players) {
	$("#leaderboard").html("");
	$("#stats").html("");
	$("#svg").html("");
	$("#legend").html("<i>Larger circles represent articles that were visited more often this game</i>");

	inContainer = d3.select("#results");
	console.log(inContainer);
	let svg = d3.select("#svg");

	let height = 500;
	let width;

	svg.attr("style:width", "100%");
	svg.attr("style:height", "" + height + "px");

	inContainer.attr("style:width", "80%");
	inContainer.attr("style:height", "" + height + "px");

	let margins = {"left" : 120, "top" : 10, "right" : 90, "bottom" : 30};
	let buffer = 0;
	width = document.getElementById("svg").getBoundingClientRect().width;

	let topDist = document.getElementById("results").getBoundingClientRect().top;
	let leftDist = document.getElementById("results").getBoundingClientRect().left;

	let playerNames = [];

	let data = [];
	let articleNamesToCount = new Map();

	// Get the information for the axes, get an array of all the player names,
	// and get data together
	let maxTime = 0;
	for (let i = 0; i < players.length; i++) {
	  player = players[i];
		console.log(player);
	  maxTime = Math.max(maxTime, player.playTime);

	  let playerName = player.name;
    playerNames.push(playerName);

	  let path = player.path;
	  for (let j = 0; j < path.length; j++) {
	    let article = path[j];
	    let articleName = path[j].page.name;
	    data.push({"playerName" : playerName, "step" : article});

	    if (!articleNamesToCount.has(articleName)) {
	      articleNamesToCount.set(articleName, 0);
	    }

	    articleNamesToCount.set(articleName, articleNamesToCount.get(articleName) + 1);
	  }
	}

	maxTime = maxTime / 60000;

	/// Followed this d3 tutorial: http://bl.ocks.org/weiglemc/6185069
	// Setup x
	let xValue = function(d) { return d.step.arrivalTime / 60000; };
	let xScale = d3.scale.linear().domain([0, maxTime]).range([margins.left, width - margins.right]);
	let xMap = function(d) { return xScale(xValue(d));};
	let xAxis = d3.svg.axis().scale(xScale).orient("bottom").outerTickSize(0);

	// Setup y
	let yValue = function(d) { return d.playerName; };
	let yScale = d3.scale.ordinal().domain(playerNames).rangePoints([margins.top, height - margins.bottom], 1.0);;
	let yMap = function(d) { return yScale(yValue(d));};
	let yAxis = d3.svg.axis().scale(yScale).orient("left").outerTickSize(0);

	let radius = function(d) {
		return 4 + Math.min(articleNamesToCount.get(d.step.page.name), 4) * (4 / Math.min(players.length, 4));
	};

	let color = function(d) {
	  let pageName = d.step.page.name;
		console.log(pageName);
		console.log(endPage);
	  if (pageName == endPage) {
	    return "#222";
	  } else {
	    return d3.scale.category10().domain(playerNames)(d.playerName);
	  }
	}

	// Draw the xAxis
	svg.append("g")
	    .attr("class", "axis")
	    .attr("transform", "translate(0, " + (height - margins.bottom) + ")")
	    .call(xAxis)
	  .append("text")
	    .attr("class", "label")
	    .attr("x", width - margins.right)
	    .attr("y", -6)
	    .style("text-anchor", "end")
	    .text("Time (min)");

	// Draw the yAxis
	svg.append("g")
	    .attr("class", "axis")
	    .attr("transform", "translate(" + (margins.left - buffer) + ", 0)")
	    .call(yAxis)

	// Draw the data
	svg.selectAll(".dot")
	  .data(data)
	.enter().append("circle")
	  .attr("class", "dot")
	  .attr("fill", color)
	  .attr("r", radius)
	  .attr("cx", xMap)
	  .attr("cy", yMap)
	  .on("mouseover", function(d) {
	    x = $(this).position().left;
	    y = $(this).position().top;

	    tooltip.transition()
			       .duration(200)
			       .style("opacity", .9);

	    tooltip.html(titleFromHref(d.step.page.name))
	      .style("left", "" + (x + 7) + "px")
	      .style("top", "" + (y - 26) + "px");

	    r = d3.select(this).attr("r");
			console.log(r);
	    d3.select(this).transition()
				.duration(200)
	      .attr("r", r * 1.2);
	  })
	  .on("mouseout", function(d) {
	    tooltip.style("opacity", 0);

		  tooltip.html("")
		 			   .style("left", "0px")
						 .style("right", "0px");

	    d3.select(this).transition()
	      .duration(200)
	      .attr("r", radius(d));
	  });

	let tooltip = d3.select("body").append("div")
	  .attr("class", "tooltip")
	  .style("opacity", 0);
}

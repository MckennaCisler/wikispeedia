let currSeconds = 0;
let setWinner = false;

let docReady = false;
let serverReady = false;

let leaderboard = true;
let recentPlayers = [];

$(document).ready(() => {
	if (serverReady) {
		serverConn.registerAllPlayers(playersCallback);
	}

	$("#disp-buttons").on("change", (event) => {
		leaderboard = event.target.id == "lead";
		console.log(recentPlayers);
		playersCallback(recentPlayers);
	});

	docReady = true;
});

serverConn.whenReadyToRecieve(() => {
	serverConn.registerError(displayServerConnErrorRedirectHome);

	if (docReady) {
		serverConn.registerAllPlayers(playersCallback);
	} else {
		serverReady = true;
	}
});

serverConn.whenReadyToSend(() => {
	serverConn.getPlayers("", playersCallback, displayServerConnError);
});

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
	if (winners.length > 0) {
		$("#stats").html(`<br><i>Winning time: ${millisecondsToStr(winners[0].playTime)}</i>`);
	}
}

function drawHistory(players) {
	$("#leaderboard").html("");
	$("#stats").html("");
}

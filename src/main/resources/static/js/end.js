let $winner;
let $leaderboard;
let fakeId = 1;

let currSeconds = 0;
let setWinner = false;

$(document).ready(() => {
});

serverConn.ready(() => {
	serverConn.registerError(displayServerConnError);

	// TODO: get everything interfacing with the server
	// serverConn.registerAllPlayers("", playersCallback, displayServerConnError);
	$winner = $("#winner");
	$leaderboard = $("#leaderboard");

	$winner.html("Loading...");
	playersCallback(fakePlayers);
});

// Updates the player to time map and redraws the results
function playersCallback(players) {
	drawResults(players);
}

function updateTime() {
	$td = $(this.find("td")[1]);
	currentTime = $td.html();
	currentTimeSplit = currentTime.split(":");
	minutes = Number(currentTimeSplit[0]);
	seconds = Number(currentTimeSplit[1]);

	seconds = seconds + 1;
	if (seconds >= 60) {
		seconds = seconds - 60;
		minutes = minutes + 1;
	}

	secondsStr = "" + seconds;
	if (seconds < 10) {
		secondsStr = "0" + secondsStr;
	}

	$td.html("" + minutes + ":" + secondsStr);
}

// Draws the results
function drawResults(players) {
	playersSorted = players.sort( function(a, b) {return a.playtime - b.playtime} );
	if (!setWinner && playersSorted.length > 0) {
		$winner.html(`<b> ${playersSorted[0].name} wins! </b>`);
	}

	for (let i = 0; i < playersSorted.length; i++) {
		let player = playersSorted[i];
		nameHtml = player.name;
		if (player.id == fakeId) {
			nameHtml = "<b>" + nameHtml + "</b>";
		}

		let style = "";
		if (i == 0) {
			$leaderboard.html("");
			style = "table-success";
		} else if (!player.done) {
			style = "table-danger";
		}

		$el = $(`<tr><td>${nameHtml}</td><td>${minutesToStr(player.playtime)}</td></tr>`)
						.appendTo($leaderboard)
						.attr("class", "" + style);

		if (!player.done) {
			window.setInterval(updateTime.bind($el), 1000);
		}
	}
}

/// fake data
let cat = {"url" : "https://www.wikipedia.org/wiki/Cat", "name" : "Cat"};
let sanskrit = {"url" : "https://www.wikipedia.org/wiki/Sanskrit", "name" : "Sanskrit"};
let hindi = {"url" : "https://www.wikipedia.org/wiki/Hindi", "name" : "Hindi"};
let english = {"url" : "https://www.wikipedia.org/wiki/English", "name" : "English"};
let spanish = {"url" : "https://www.wikipedia.org/wiki/Spanish", "name" : "Spanish"};
let portuguese = {"url" : "https://www.wikipedia.org/wiki/Portuguese", "name" : "Portuguese"};
let german = {"url" : "https://www.wikipedia.org/wiki/German", "name" : "German"};
let italian = {"url" : "https://www.wikipedia.org/wiki/Italian", "name" : "Italian"};
let latin = {"url" : "https://www.wikipedia.org/wiki/Latin", "name" : "Latin"};
let hebrew = {"url" : "https://www.wikipedia.org/wiki/Hebrew", "name" : "Hebrew"};
let yiddish = {"url" : "https://www.wikipedia.org/wiki/Yiddish", "name" : "Yiddish"};
let bengali = {"url" : "https://www.wikipedia.org/wiki/Bengali", "name" : "Bengali"};
let icelandic = {"url" : "https://www.wikipedia.org/wiki/Icelandic", "name" : "Icelandic"};
let greek = {"url" : "https://www.wikipedia.org/wiki/Greek", "name" : "Greek"};
let dog = {"url" : "https://www.wikipedia.org/wiki/Dog", "name" : "Dog"};

let player1 = {"id" : 1, "name" : "rohan", "done" : true, "startPage" : cat, "endPage" : dog, "path" : [cat, dog], "playtime" : 0.4};
let player2 = {"id" : 2, "name" : "mckenna", "done" : true, "startPage" : cat, "endPage" : dog, "path" : [cat, english, spanish, dog], "playtime" : 1};
let player3 = {"id" : 3, "name" : "sean", "done" : true, "startPage" : cat, "endPage" : dog, "path" : [cat, english, spanish, dog], "playtime" : 1.4};
let player4 = {"id" : 4, "name" : "jacob", "done" : true, "startPage" : cat, "endPage" : dog, "path" : [cat, english, spanish, greek, italian, portuguese, spanish, dog], "playtime" : 1.4};

let fakePlayers = [player1, player2, player3, player4];

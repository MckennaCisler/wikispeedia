/// Fills in the text fields in play.html

///
// Globals
///

// TODO: Maybe some race conditions here
const $updates = $("#updates");
const $destination = $("#destination");
const $timer = $("#timer");

const $title = $("#title");
const $article = $("#article");

// Player info
let playerPaths = new Map();

// Game info
let currHref; // the current article
let hasDrawnPlayerList;
let docReady = false;
let setDestinationWhenReady = false;
let destPage;

// Time
let startTime = new Date().getTime();

// TODO: Wait for server to be ready
// TODO: Create a page run script that runs earlier
$(document).ready(() => {
	docReady = true;
	resize();
	$timer.text("0:00");
	$title.html("<b>Loading...</b>");
	$destination.html("<b>Loading...</b>");
	$updates.append("<i>Game started</i>");
	setInterval(updateTimer, 200);

	if (setDestinationWhenReady) {
		$destination.html("<b>" + destPage + "</b>");
	}
});

$(window).resize(resize);

function resize() {
	"use strict";
	if ($(window).width() <= 550) {
		$('#info').removeClass("col-4");
		$('#article-col').removeClass("col-8");
	} else {
		$('#info').addClass("col-4");
		$('#article-col').addClass("col-8");
	}
}

serverConn.whenReadyToRecieve(() => {
    serverConn.registerError(displayServerConnErrorRedirectHome);
		serverConn.registerAllPlayers(newUpdate);
    serverConn.registerEndGame(() => {
		setCookie('timePlayed', audio.currentTime);
        window.location.href = "end";
    });
});

serverConn.whenReadyToSend(() => {
    hasDrawnPlayerList = false;
    currHistory = serverConn.clientId; // the player whose history is currently displayed

		// wait until we have client id to register this one
    // serverConn.getPlayers(newUpdate);
    serverConn.goToInitialPage(drawPage, errorPage);
		serverConn.getSettings("", GAME_STATE.STARTED, settingsCallback, settingsError);

		window.onpopstate = (function(event) {
			if (event.state.href !== undefined) {
		  	serverConn.goBackPage(event.state.href, drawPage, errorPage);
			} else {
				console.log("Unknown state was popped");
			}
		}).bind(this);
});

///
// Settings
///
function settingsCallback(settings) {
	console.log("SETTINGS");
	console.log(settings);
	console.log("HEREHERHERE");

	destPage = titleFromHref(settings.goalPage.name);
	if (docReady) {
		$destination.html("<b>" + destPage + "</b>");
	} else {
		setDestinationWhenReady = true;
	}
}

function settingsError(error) {
	console.log(error);
	displayError("Couldn't get the game settings");
}

///
// Links
///

// Link is clicked
function goToPage(href) {
  serverConn.gotoPage(href, drawPage, errorPage);
}

// Callback to the server Request
function drawPage(page) {
  html = page.text;
  href = page.href;
  title = titleFromHref(href);

	// add to browser history if not already there
	if (history.state == null || history.state.href !== href) {
		history.pushState({"href": href}, "", "");
	}

  if (href != currHref) {
    $title.html("<b>" + title + "</b>");
    $article.html(html);
    $article.scrollTop(0);
    cleanHtml();

    currHref = href;
  }
}

// Error callback
function errorPage(error) {
  displayError("Couldn't go to page: " + error.error_message);
	drawPage(error.payload);
}

// Replaces the links with callbacks
function cleanHtml() {
  $article.find("a").each(function(index, element) {
    // Adding a "" because attributes are not strings
    let link = "" + $(element).attr("href");
    $(element).attr("id", "link");
    if (link.charAt(0) != "#") {
      $(element).attr("href", hrefHelper("goToPage", link));
    }
  });

  $article.find("img").each(function(index, element) {
    let src = "" + $(element).attr("src");
    // console.log(src);

    if (!src.startsWith("https:")) {
      $(element).attr("src", "https:" + src);
    }

    // Make sure this is rigorous
    let srcset = "" + $(element).attr("srcset");
    if (typeof srcset != typeof undefined && srcset != false) {
      $(element).attr("srcset", replaceAll(srcset, "//", "https://"));
    }
  });
}

///
// History
///

function newUpdate(players) {
  for (let i = 0; i < players.length; i++) {
    let player = players[i];
		let newPath = player.path;
		let oldPath = playerPaths.get(player.id);
		let oldPathLength = 0;

		if (oldPath != undefined) {
			oldPathLength = oldPath.length;
		}

		for (let j = oldPathLength; j < newPath.length; j++) {
			page = newPath[j];
			if (j != 0) {
	 			if (player.id == serverConn.clientId) {
					$updates.prepend(`<b>You visited ${"\"" + titleFromHref(page.page.name) + "\""}<br></b>`);
				} else {
					$updates.prepend(player.name + ` visited ${"\"" + titleFromHref(page.page.name) + "\""}<br>`);
				}
			}
		}

    playerPaths.set(player.id, player.path);
  }
}

///
// Timer
///

function updateTimer() {
  currentTime = new Date().getTime() - startTime;
  totalSeconds = Math.floor(currentTime / 1000);
  minutes = Math.floor(totalSeconds / 60);
  seconds = totalSeconds - (minutes * 60);
  secondsStr = "" + seconds;
  if (seconds < 10) {
      secondsStr = "0" + secondsStr;
  }

  $timer.text(minutes + ":" + secondsStr);
}

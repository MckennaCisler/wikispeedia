/// Fills in the text fields in play.html

///
// Globals
///

const $history = $("#history");
const $destination = $("#destination");
const $timer = $("#timer");
const $historyDropdown = $("#history-dropdown");
const $historyDropdownList = $("#history-dropdown-list");

const $title = $("#title");
const $article = $("#article");

let ding = new Audio('lib/assets/ding.mp3');
let currHistory;
let currHistoryName;

// Player info
let playerPaths = new Map();

// Game info
let startHref = "https://en.wikipedia.org/wiki/Cat"; // the start article
let currHref; // the current article
let destHref = "https://en.wikipedia.org/wiki/Dog"; // the end article
let hasDrawnPlayerList;

// Time
let startTime = new Date().getTime();

// TODO: Wait for server to be ready
// TODO: Create a page run script that runs earlier
$(document).ready(() => {
	resize();
	$timer.text("0:00");
	$title.html("<b>Loading...</b>");
	$destination.html("<b>Loading...</b>");
	setInterval(updateTimer, 200);
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
    serverConn.registerError(displayServerConnError);
    serverConn.registerEndGame(() => {
		setCookie('timePlayed', audio.currentTime);
        window.location.href = "end";
    });
});

serverConn.whenReadyToSend(() => {
    hasDrawnPlayerList = false;
    // $destination.html("<b>" + titleFromHref(end) + "</b>");
    currHistory = serverConn.clientId; // the player whose history is currently displayed

		// wait until we have client id to register this one
		serverConn.registerAllPlayers(drawHistoryCallback);

    serverConn.getPlayers(drawHistoryCallback);
    serverConn.goToInitialPage(drawPage, errorPage);
});

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

  if (href != currHref) {
    $title.html("<b>" + title + "</b>");
    $article.html(html);
    $article.scrollTop(0);
    cleanHtml();

    if (currHistory == serverConn.clientId) {
      drawHistory();
    }

    currHref = href;
  }
}

// Error callback
function errorPage(error) {
  displayError("Couldn't go to page: " + error.error_message);
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
    console.log(src);

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

function historyChange(newHistory, newHistoryName) {
  currHistory = newHistory;
  currHistoryName = newHistoryName;
  drawHistory();
}

function drawHistoryCallback(players) {
  for (let i = 0; i < players.length; i++) {
    player = players[i];
    playerPaths.set(player.id, player.path);

    if (!hasDrawnPlayerList) {
      // Something like this: <li><a href="javascript:historyChange('Player 1')"><b>Me</b></a></li>
      playerHtml = "";
      if (player.id == serverConn.clientId) {
        playerHtml = "<b>Me</b>";
      } else {
        playerHtml = player.name;
      }

      $historyDropdownList.append(
        "<li><a href=\""
        + hrefHelper("historyChange", player.id + "', '" + player.name)
        + "\">" + playerHtml + "</a></li>");
    }
  }

  if (!hasDrawnPlayerList) {
    hasDrawnPlayerList = true;
  }

  drawHistory();
}

function drawHistory() {
  playerHistory = playerPaths.get(currHistory);
  html = "";
  if (playerHistory.path.length > 0) {
    startIndex = 0;
    if (playerHistory.path.length > 8) {
      startIndex = playerHistory.path.length - 6;
      html = html + "<i>(" + startIndex + " articles before)</i><br>";
    }

    for (i = startIndex; i < playerHistory.path.length - 1; i++) {
      html = html + titleFromHref(playerHistory.path[i].url) + "<br>";
    }

    html = html + "<b>" + titleFromHref(playerHistory[playerHistory.path.length - 1].url) + "</b>";
  }

  $history.html(html);

  if (currHistory != serverConn.clientId) {
    $historyDropdown.html(currHistoryName + "'s progress");
  } else {
    $historyDropdown.html("<b>My progress</b>");
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

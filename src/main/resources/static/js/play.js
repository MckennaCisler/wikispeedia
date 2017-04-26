/// Fills in the text fields in play.html

///
// Globals
///

const $history = $("#history");
const $destination = $("#destination");
const $timer = $("#timer");
const $historyDropdown = $("#history-dropdown");
const $historyDropdownList = $("#player-history-list");

const $title = $("#title");
const $article = $("#article");

let ding = new Audio('lib/assets/ding.mp3');

// Player info
let myId;
let playerPaths = new Map();

// Game info
let currHistory = myId; // the player whose history is currently displayed
let startHref = "https://en.wikipedia.org/wiki/Cat"; // the start article
let currHref; // the current article
let destHref = "https://en.wikipedia.org/wiki/Dog"; // the end article
let hasDrawnPlayerList = false;

// Time
let startTime = new Date().getTime();

// TODO: Wait for server to be ready
// TODO: Create a page run script that runs earlier
$(document).ready(() => {
  console.log("something");

  $timer.text("0:00");
  $title.html("<b>Loading...</b>");

  $destination.html("<b>" + titleFromHref(destHref) + "</b>");
  setInterval(updateTimer, 200);
});

serverConn.ready(() => {
    serverConn.registerEndGame(() => {
        window.location.href = "end";
    });

    myId = serverConn.clientId;
    serverConn.registerAllPlayers(drawHistoryCallback);
    goToPage(startHref);
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

    history.push(title);
    if (currHistory == myId) {
      drawHistory();
    }

    currHref = href;
  }
}

// Error callback
function errorPage() {
  displayError("Couldn't go to page");
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

function historyChange(newHistory) {
  currHistory = newHistory;
  drawHistory();
}

function drawHistoryCallback(players) {
    console.log("Made it here");

    if (!hasDrawnPlayerList) {
      for (player of players) {
        // Something like this: <li><a href="javascript:historyChange('Player 1')"><b>Me</b></a></li>
        playerHtml = player.id;
        if (myId == player.id) {
          playerHtml = "<b>" + playerHtml + "</b>";
        }

        $historyDropdownList.append(
          "<li><a"
          + hrefHelper("historyChange", player.id)
          + ">" + playerHtml + "</a></li>");
      }

      hasDrawnPlayerList = true;
    }

    for (player of players) {
      playerPaths.set(player.id, player.path);
    }

    drawHistory;
}

function drawHistory() {
  history = playersPaths.get(currHistory);
  html = "";
  if (history.length > 0) {
    startIndex = 0;
    if (history.length > 8) {
      startIndex = history.length - 6;
      html = html + "<i>(" + startIndex + " articles before)</i><br>";
    }

    for (i = startIndex; i < history.length - 1; i++) {
      html = html + history[i] + "<br>";
    }

    html = html + "<b>" + history[history.length - 1] + "</b>";
  }

  $history.html(html);

  if (currHistory != myId) {
    $historyDropdown.html(currHistory + "'s progress");
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

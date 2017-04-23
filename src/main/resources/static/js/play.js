/// Fills in the text fields in play.html

///
// Globals
///

const $history = $("#history");
const $destination = $("#destination");
const $timer = $("#timer");
const $historyDropdown = $("#history-dropdown");

const $title = $("#title");
const $article = $("#article");

let ding = new Audio('lib/assets/ding.mp3');

// Player info
let username = "Player 1";
let history = [];

// Game info
let currHistory = username; // the player whose history is currently displayed
let startHref = "https://en.wikipedia.org/wiki/Cat"; // the start article
let currHref; // the current article
let destHref = "https://en.wikipedia.org/wiki/Dog"; // the end article

// Time
let startTime = new Date().getTime();

// TODO: Wait for server to be ready
// TODO: Create a page run script that runs earlier
$(document).ready(() => {
  $timer.text("0:00");
  $title.html("<b>Loading...</b>");

  $destination.html("<b>" + titleFromHref(destHref) + "</b>");
  setInterval(updateTimer, 200);

});

serverConn.ready(() => {
    serverConn.registerEndGame(() => {
        window.location.href = "end";
    });

    serverConn.registerAllPlayers(drawPlayers);

    goToPage(startHref);
});

function drawPlayers(players) {
    // TODO: Display player paths in sidebar
}


///
// Links
///

// Link is clicked
function goToPage(href) {
  serverConn.gotoPage(href, drawPage, errorPage);
}

// Callback to the server Request
function drawPage(message) {
  payload = message.payload;
  html = payload.text;
  href = payload.href;
  title = titleFromHref(href);

  if (href != currHref) {
    $title.html("<b>" + title + "</b>");
    $article.html(html);
    $article.scrollTop(0);
    cleanHtml();

    history.push(title);
    if (currHistory == username) {
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
    if (link.charAt(0) != "#") {
      $(element).attr("href", hrefHelper(link));
    }
  });
}

// Helper to get callback
function hrefHelper(href) {
  return "javascript:goToPage('" + href + "')";
}

///
// History
///

function historyChange(newHistory) {
  currHistory = newHistory;
  drawHistory();
}

function drawHistory() {
  $history.html(getPlayerHistory(currHistory));

  if (currHistory != username) {
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

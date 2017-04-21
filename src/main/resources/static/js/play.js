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
let startTitle = "Cat"; // the start article
let currTitle; // the current title
let destTitle = "Dog"; // the end article

// Time
let startTime = new Date().getTime();

$(document).ready(() => {
  setInterval(updateTimer, 500);
  $timer.text("0:00");

  $destination.html(destTitle);

  goToLink(startTitle);
});


///
// Links
///

// Link is clicked
function goToLink(title) {
  if (title != currTitle) {
    $title.html("<b>" + title + "</b>");
    $article.html(getArticleHtmlTemp(title));
    currTitle = title;
    history.push(currTitle);
    drawHistory();

    if (title == destTitle) {
      ding.play();
    }
  }
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

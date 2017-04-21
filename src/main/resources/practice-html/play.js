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

let ding = new Audio('assets/ding.mp3');

let username = "Player 1";
let currHistory = username; // the player whose history is currently displayed
let currTitle = "Cat"; // the start article
let destTitle = "Dog"; // the end article
let startTime = new Date().getTime();
let history = [];

$(document).ready(() => {
  setInterval(updateTimer, 500);
  $timer.text("0:00");
  $title.html("<b>" + currTitle + "</b>");
  $article.html(getArticleHtmlTemp(currTitle));
  $destination.html("<b>" + destTitle + "</b>");
  history.push(currTitle);
  drawHistory();
	$("#info-col").matchHeight();
});


///
// Links
///

// Link is clicked
function linkClick(title) {
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

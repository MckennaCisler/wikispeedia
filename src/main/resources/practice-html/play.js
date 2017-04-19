/// Fills in the text fields in play.html

///
// Globals
///

const $history = $("#history");
const $destination = $("#destination");
const $timer = $("#timer");

const $title = $("#title");
const $article = $("#article");

let ding = new Audio('assets/ding.mp3');

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
  $history.html(updateHistory(currTitle));
});


///
// Links
///

// Link is clicked
function linkClick(title) {
  if (title != currTitle) {
    $title.html("<b>" + title + "</b>");
    $article.html(getArticleHtmlTemp(title));
    updateHistory(title);
    currTitle = title;

    if (title == destTitle) {
      ding.play();
    }
  }
}

///
// History
///

// Update history
function updateHistory(title) {
  history.push(title);
  historyString = "";

  if (history.length > 0) {
    startIndex = 0;
    if (history.length > 12) {
      startIndex = history.length - 9;
      historyString = "<i>(" + startIndex + " articles before)</i> - ";
    }

    for (i = startIndex; i < history.length - 1; i++) {
      historyString = historyString + history[i] + " - ";
    }

    historyString = historyString + "<b>" + history[history.length - 1] + "</b>";
  }

  $history.html(historyString);
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

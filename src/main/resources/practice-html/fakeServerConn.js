// The actual getHtml will call the backend, but it'll also need to "hijack"
// the links to trigger a function that we've defined
// The code will look something like this:
// "<a href=\"javascript:linkClick()\">someLink</a>"
function getArticleHtmlTemp(articleTitle) {
  html = "";
  if (articleTitle == "Cat") {
    html = html + linkHelper("Dog", "Dogs") + " like to play "
            + linkHelper("Football", "football") + ".";
  } else if (articleTitle == "Dog") {
    html = html + "Ahhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh. "
            + "I don't like " + linkHelper("Cat", "cats") + "!";
  } else if (articleTitle == "Football") {
    html = html + linkHelper("Cat", "Cats") + " or " + linkHelper("Dog", "dogs")
            + " hmmmmmmmmmmmm.";
  }

  return html;
}

function getPlayerHistory(playerName) {
  html = "";
  if (playerName == "Player 1") {
    html = "";
    if (history.length > 0) {
      startIndex = 0;
      if (history.length > 12) {
        startIndex = history.length - 9;
        html = html + "<i>(" + startIndex + " articles before)</i> - ";
      }

      for (i = startIndex; i < history.length - 1; i++) {
        html = html + history[i] + " - ";
      }

      html = html + "<b>" + history[history.length - 1] + "</b>";
    }
  } else if (playerName == "Player 2") {
    html = html + "<b>Cat</b>";
  } else if (playerName == "Player 3") {
    html = html + "<b>Cat</b>";
  }

  return html;
}

// Helper to get an "internal link"
function linkHelper(param, text) {
  return "<a href=\"javascript:linkClick('" + param + "')\">" + text + "</a>";
}

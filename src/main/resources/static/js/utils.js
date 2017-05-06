// TODO: Make this escape more
function titleFromHref(href) {
  result = href.replace("https://en.wikipedia.org/wiki/", "");
  result = replaceAll(result, "_", " ");
  return result;
}

// from StackOverflow:
// http://stackoverflow.com/questions/1144783/how-to-replace-all-occurrences-of-a-string-in-javascript?page=1&tab=votes#tab-top
function replaceAll(str, find, replace) {
  return str.replace(new RegExp(find, 'g'), replace);
}

function assert(statement, ifFalse, ifTrue) {
    if (statement === false) {
        if (ifFalse !== undefined) {
            console.log("ASSERTION ERROR - See stack trace");
        } else {
            console.log("ASSERTION ERROR - " + msg);
        }
    } else {
        if (ifTrue !== undefined) {
            console.log(ifTrue);
        }
    }
}

function firstSentence(paragraph) {
  return paragraph.split(".")[0];
}

// Helper to get callback
function hrefHelper(f, href) {
  return "javascript:" + f + "('" + href + "')";
}

function displayError(msg, error_code) {
    my_alert(msg);
    //alert(msg);
}

function displayErrorRedirect(msg, redirect, error_code) {
    my_alert_cb(msg, () => {
      window.location.href = redirect;
    });
}

function clearError() {
    // TODO
}

function displayServerConnErrorRedirectHome(error) {
    displayErrorRedirect(error.error_message, "/", error.error_code);
}

function displayServerConnError(error) {
    displayErrorRedirect(error.error_message, error.error_code);
}

function minutesToStr(minutes) {
  let seconds = Math.floor(((minutes - Math.floor(minutes)) * 60));
  if (seconds < 10) {
    seconds = "0" + seconds;
  }

  return "" + Math.floor(minutes) + ":" + seconds;
}

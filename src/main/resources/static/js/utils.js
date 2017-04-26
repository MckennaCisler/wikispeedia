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


// Helper to get callback
function hrefHelper(f, href) {
  return "javascript:" + f + "('" + href + "')";
}

function displayError(msg) {
    // TODO
    alert(msg);
}

function clearError() {
    // TODO
}

function displayServerConnError(error) {
    displayError(error.error_message);
}

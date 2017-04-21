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

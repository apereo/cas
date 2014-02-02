function getActiveDocumentationVersionInView() {
	var currentVersion = "current";
	var href = location.href;
    var index = href.indexOf("/_site/");

    if (index == -1) {
		var uri = new URI(document.location);

		if (uri.segment(1) != null) {
	  		currentVersion = uri.segment(1);
		}
	  } else {
		href = href.substring(index + 7);
		index = href.indexOf("/");
		currentVersion = href.substring(0, index);
	}
	return currentVersion;
}

function loadSidebarForActiveVersion() {
	var uri = new URI(document.location);
	$("#sidebartoc").load("/" + uri.segment(0) + "/" + getActiveDocumentationVersionInView() + "/sidebar.html");
}

function generateSidebarLinksForActiveVersion() {
	var uri = new URI(document.location);
	$('a').each(function() {
		var href = this.href;
		if (href.indexOf("$version") != -1) {
			href = href.replace("$version", uri.segment(0) "/" + getActiveDocumentationVersionInView());
			$(this).attr('href', href);
		}
  	});
}

$(function() {
	loadSidebarForActiveVersion();
});
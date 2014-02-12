var CONST_CURRENT_VER = "current";

function getActiveDocumentationVersionInView() {
	var currentVersion = CONST_CURRENT_VER;
	var href = location.href;
    var index = href.indexOf("/_site/");

    if (index == -1) {
		var uri = new URI(document.location);

		if (uri.filename() != uri.segment(1) && uri.segment(1) != "developer") {
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
	$("#sidebartoc").load("/cas/" + getActiveDocumentationVersionInView() + "/sidebar.html");
}

function generateSidebarLinksForActiveVersion() {
	$('a').each(function() {
		var href = this.href;
		if (href.indexOf("$version") != -1) {
			href = href.replace("$version", "cas/" + getActiveDocumentationVersionInView());
			$(this).attr('href', href);
		}
  	});
}

$(function() {
	loadSidebarForActiveVersion();

	var formattedVersion = getActiveDocumentationVersionInView();
	if (formattedVersion != "" && formattedVersion.indexOf(CONST_CURRENT_VER) == -1) {
		formattedVersion = " (" + formattedVersion + ")"
	} else {
		formattedVersion = "";
	}
	document.title = $("h1").first().text() + formattedVersion;
});
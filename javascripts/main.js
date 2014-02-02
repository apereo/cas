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
});
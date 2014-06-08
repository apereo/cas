var CONST_CURRENT_VER = "development";

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

function generateToolbarIcons() {
	var CAS_REPO_URL_GITHUB = $('#forkme_banner').attr('href');

	var uri = new URI(document.location);
	var segments = uri.segment();
	var page = "";

	for (var i = 1; i < segments.length; i++) {
		page += segments[i] + "/";    
	}


	var editLink = CAS_REPO_URL_GITHUB + "/edit/gh-pages/" + page;
	$('#toolbarIcons').append("<a target='_blank' href='" + editLink +
		"'><img src='/cas/images/edit.png' alt='Edit with Github' title='Edit with Github'></a>");

	var historyLink = CAS_REPO_URL_GITHUB + "/commits/gh-pages/" + page;
	$('#toolbarIcons').append("<a target='_blank' href='" + historyLink +
		"'><img src='/cas/images/history.png' alt='View commit history on Github' title='View commit history on Github'>");

	var deleteLink = CAS_REPO_URL_GITHUB + "/delete/gh-pages/" + page;
	$('#toolbarIcons').append("<a target='_blank' href='" + deleteLink +
		"'><img src='/cas/images/delete.png' alt='Delete with Github' title='Delete with Github'>");
}

function generateTableOfContentsForPage() {
  $('#tableOfContents').append("<strong>Table of Contents</strong>");
  $('#tableOfContents').append("<ul>");
  
  $('h1, h2, h3').each(function() {
    if (this.id != "project_tagline" && this.id != "") {
      var tagName = $(this).prop("tagName").trim();
      var currentIndex = parseInt(tagName.substring(1, 2));
      
      var alignment = "";

      switch (currentIndex) {
        case 2:
          for (var i = 0; i < 3; i++) { alignment += "&nbsp;" }
          break;
        case 3:
          for (var i = 0; i < 6; i++) { alignment += "&nbsp;" }
          break;
      }
      
      $('#tableOfContents').append("<li><a href='#" + this.id + "'>" + alignment +
          this.innerText + "</a></li>");
    }
  });
  $('#tableOfContents').append("</ul>");
}

$(function() {
	loadSidebarForActiveVersion();
    generateTableOfContentsForPage();
  	generateToolbarIcons();

	var formattedVersion = getActiveDocumentationVersionInView();
	if (formattedVersion != "" && formattedVersion.indexOf(CONST_CURRENT_VER) == -1) {
		formattedVersion = " (" + formattedVersion + ")"
	} else {
		formattedVersion = "";
	}
	document.title = $("h1").first().text() + formattedVersion;
  
});


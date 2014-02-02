 
$(function(){
  	  var currentVersion = "current";
  	  var href = location.href;
      var index = href.indexOf("/_site/");

      if (index == -1) {
      	var uri = new URI(document.location);
  	  	currentVersion = uri.segment(1);
  	  	$("#sidebartoc").load("/cas/" + currentVersion + "/sidebar.html");
  	  } else {
        href = href.substring(index + 7);
        index = href.indexOf("/");
        
        currentVersion = href.substring(0, index);
      }

      document.title = document.title + " - Version " + currentVersion;
});

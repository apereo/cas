---
layout: default
title: CAS - User Interface Customization
---

#Overview
Branding the CAS User Interface (UI) involves simply editing the CSS stylesheet and also a small collection of relatively simple JSP include files, also known as views. Optionally, you may also wish to modify the text displayed and/or add additional Javascript effects on these views.

All the files that we'll be discussing in this section that concern the theme are located in and referenced from: `/cas-server-webapp/src/main/webapp`.

#Browser Support
CAS user interface should properly and comfortably lend itself to all major browser vendors:
* Google Chrome
* Mozilla Firefox
* Apple Safari
* Microsoft Internet Explorer

Note that certain older version of IE, particularly IE 9 and below may impose additional difficulty in getting the right UI configuration in place.

#Getting Started
##CSS
The default styles are all contained in a single file located in `css/cas.css`. This location is set in `WEB-INF/classes/cas-theme-default.properties`. If you would like to create your own `css/custom.css file`, for example, you will need to update `standard.custom.css.file` key in that file.

{% highlight bash %}
standard.custom.css.file=/css/cas.css
cas.javascript.file=/js/cas.js
{% endhighlight %}

###Responsive Design
CSS media queries bring responsive design features to CAS which would allow adopter to focus on one theme for all appropriate devices and platforms. These queries are defined in the same `css/cas.css` file. Below follows an example:

{% highlight css %}
@media only screen and (max-width: 960px) { 
  footer { padding-left: 10px; }
}

@media only screen and (max-width: 799px) { 
  header h1 { font-size: 1em; }
  #login { float: none; width: 100%; }
  #fm1 .row input[type=text], 
  #fm1 .row input[type=password] { width: 100%; padding: 10px; box-sizing: border-box; -webkit-box-sizing: border-box; -moz-box-sizing: border-box; }
  #fm1 .row .btn-submit { outline: none; -webkit-appearance: none; -webkit-border-radius: 0; border: 0; background: #210F7A; color: white; font-weight: bold; width: 100%; padding: 10px 20px; -webkit-border-radius: 3px; -moz-border-radius: 3px; border-radius: 3px; }
  #fm1 .row .btn-reset { display: none; }
  #sidebar { margin-top: 20px; }
  #sidebar .sidebar-content { padding: 0; }
}
{% endhighlight %}

##Javascript
If you need to add some JavaScript, feel free to append `js/cas.js`.

You can also create your own `custom.js` file, for example, and call it from within `WEB-INF/view/jsp/default/ui/includes/bottom.jsp` like so:

	<script type="text/javascript" src="<c:url value="/js/custom.js" />"></script> 

If you are developing themes per service, each theme also has the ability to specify a custom `cas.js` file under the `cas.javascript.file` setting. 

The following Javascript libraries are utilized by CAS automatically:

* JQuery
* JQuery UI
* [JavaScript Debug](http://benalman.com/projects/javascript-debug-console-log/): A simple wrapper for `console.log()`

### JSP
The default views are found at `WEB-INF/view/jsp/default/ui/`.

Notice `top.jsp` and `bottom.jsp` include files located in the `../includes` directory. These serve as the layout template for the other JSP files, which get injected in between during compilation to create a complete HTML page.

The location of these JSP files are configured in `WEB-INF/classes/default_views.properties`.

####Tag Libraries
The following JSP tag libraries are used by the user interface:

{% highlight jsp %}
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
{% endhighlight %}

<%@ page session="true" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<title>JA-SIG Central Authentication Service (CAS)</title>
		<meta name="keywords" content="Central Authentication Service,JA-SIG,CAS" />
		<link rel="stylesheet" href="css/home.css" type="text/css" media="all" />
		<link rel="stylesheet" href="css/jasig.css" type="text/css" media="all" />
		<script src="js/common.js" type="text/javascript"></script>
	</head>
	<body onload="init();">
		<div id="jasig-banner">
			<div id="jasig-logo"><a href="http://www.ja-sig.org"><img src="images/jasig-logo.gif" width="71" height="19" alt="JA-SIG logo" /></a></div>
			<form action="http://www.google.com/u/jasig" method="get" style="display:inline;margin:0;padding:0;">
			<ul id="jasig-nav">
				<li><a href="http://www.ja-sig.org/products/">Products</a></li>
				<li><a href="http://www.ja-sig.org/conferences.html">Conferences</a></li>
				<li><a href="http://clearinghouse.ja-sig.org/">Clearinghouse</a></li>
				<li><a href="http://www.ja-sig.org/facebook/">Community</a></li>
				<li><a href="http://developer.ja-sig.org">Developers</a></li>
				<li>
					<input value="ja-sig.org" name="domains" type="hidden" />
					<input value="ja-sig.org" name="sitesearch" type="hidden" />
					<input style="font-size: 11px;" maxlength="255" name="q" type="text" />
					<input name="sa" type="image" src="./images/input-search.gif" value="search" style="vertical-align: top;" />
				</li>
			</ul>
			</form>
		</div>
		<div id="header">
			<img src="./images/banner.png" width="687" height="76" alt="Central Authentication Service" />
		</div>
		<div id="content">
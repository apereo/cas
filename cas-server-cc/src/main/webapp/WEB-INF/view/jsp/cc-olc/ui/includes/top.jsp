<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
	<HEAD>
    <%@ page session="true" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<spring:theme code="mobile.custom.css.file" var="mobileCss" text="" />
	<TITLE>Connecticut College Online Community - Login/Logout</TITLE>
        <c:choose>
           <c:when test="${not empty requestScope['isMobile'] and not empty mobileCss}">
                <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;" />
                <meta name="apple-mobile-web-app-capable" content="yes" />
                <meta name="apple-mobile-web-app-status-bar-style" content="black" />
                <link type="text/css" rel="stylesheet" media="screen" href="<c:url value="/css/fss-framework-1.1.2.css" />" />
                <link type="text/css" rel="stylesheet" href="<c:url value="/css/fss-mobile-${requestScope['browserType']}-layout.css" />" />
                <link type="text/css" rel="stylesheet" href="${mobileCss}" />
           </c:when>
           <c:otherwise>
                <spring:theme code="standard.custom.css.file" var="customCssFile" />
                <link type="text/css" rel="stylesheet" href="<c:url value="${customCssFile}" />" />
           </c:otherwise>
        </c:choose>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	    <link rel="icon" href="<c:url value="/favicon.ico" />" type="image/x-icon" />
        
        <meta name="keywords" content="Connecticut College, connecticut college,biological sciences,liberal arts and sciences,premed program,pre-health advising,medical school advising,science center at connecticut college,sciences and the liberal arts,student-faculty research,undergraduates science research" />
        <meta name="description" content="Connecticut College: Within the context of a premier liberal arts curriculum, science majors at Connecticut College acquire a powerful formula for success as they enter graduate or medical degree programs or careers in science. " />
    
        <meta http-equiv="x-ua-compatible" content="IE=EmulateIE7"/>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        
        <link type="text/css" href="https://www.alumniconnections.com/olc/styles/CTC/gcssreset.css" rel="StyleSheet" />
        <link type="text/css" href="https://www.alumniconnections.com/olc/styles/CTC/gcssbase.css" rel="StyleSheet" />
        <link type="text/css" href="https://www.alumniconnections.com/olc/styles/CTC/gcss.css" rel="StyleSheet" />
        <script type="text/javascript" src="https://www.alumniconnections.com/olc/scripts/CTC/11136.js"></script>
        <script type="text/javascript" src="https://www.alumniconnections.com/olc/scripts/CTC/11135.js"></script>
        <script type="text/javascript" src="https://www.alumniconnections.com/olc/scripts/CTC/11134.js"></script>
    
        <script type="text/javascript" src="https://www.alumniconnections.com/olc/scripts/CTC/slider.js"></script>
        <script type="text/javascript" src="https://www.alumniconnections.com/olc/scripts/CTC/lightbox.js"></script>
        <script type="text/javascript" src="https://www.alumniconnections.com/olc/scripts/CTC/11133.js"></script>
        <script type="text/javascript" src="https://www.alumniconnections.com/olc/scripts/CTC/11132.js"></script>
        <script type="text/javascript" src="https://www.alumniconnections.com/olc/scripts/CTC/11131.js"></script>
        <script type="text/javascript" src="https://www.alumniconnections.com/olc/scripts/CTC/11130.js"></script>
    
        <!--[if IE 6]>
            <script type="text/javascript" src="https://www.alumniconnections.com/olc/scripts/CTC/11137.js"></script>
        <![endif]-->
        <script src="https://www.alumniconnections.com/olc/scripts/CTC/urchin.js" type="text/javascript"></script>
        <script type="text/javascript">
            urchinTracker();
        </script>
        <script language="javascript" type="text/javascript">
            var hasCookie;
            
            hasCookie = Get_Cookie( "SA_USER_OLC" );
            
            function Get_Cookie( name )
            {
                var start = document.cookie.indexOf( name + "=" );
                var len = start + name.length + 1;
                if ( ( !start ) &&
                    ( name != document.cookie.substring( 0, name.length ) ) )
                {
                    return false;
                }
                if ( start == -1 )
                {
                    return false;
                }
                return( true );
            }
        </script>
    
    <!-- OLC Global Stylesheet -->
    <link rel="stylesheet" href="https://www.alumniconnections.com/olc/styles/CTC/olc_global_styles.css">
    <!-- From contentarea102.html:  END -->              
        
	</head>
    
    <body id="cas" class="olc" onload="">
    
<!-- Basic Body -->
<!-- From contentarea105.html: END -->



<!-- From headernfo.html:  START -->
<div id="header">
	<div class="wrapper">
		<div class="headerTop">

			<div class="selectBox" onmouseover="ResourcesHide(1);" onmouseout="ResourcesHide(0);" style="width: 170px;"> <a id="adSelectBox" href="#">Resources For</a>
				<ul style="display: none;" id="selectResources">
					<li class="first"><a href="http://www.conncoll.edu/students/students.htm">Students</a></li>
					<li><a href="http://www.conncoll.edu/admission">Prospective Students</a></li>
					<li><a href="http://www.conncoll.edu/facstaff/index.htm">Faculty &amp; Staff</a></li>

					<li><a href="http://www.conncoll.edu/alumni/index.htm">Alumni</a></li>
					<li><a href="http://www.conncoll.edu/parents/index.htm">Parents &amp; Families</a></li>
					<li><a href="http://www.conncoll.edu/community/243.htm">Community &amp; Visitors</a></li>
				</ul>
			</div>

			<ul class="utilityLinks">
				<li><a href="http://www.conncoll.edu/employment/1195.htm">Employment</a></li>
				<li><a href="http://www.conncoll.edu/is/index.htm">Libraries/Technology</a></li>
				<li><a href="http://www.conncoll.edu/admission/11163.htm">Visiting Campus</a></li>
				<li><a href="http://www.conncoll.edu/events">Events</a></li>
				<li><a href="http://www.conncoll.edu/news/index.htm">News</a></li>

			</ul>
			<div class="login">
				<script language="javascript" type="text/javascript">
					if( hasCookie == true )
					{
						document.write('<a style="background:url(https://www.alumniconnections.com/olc/images/CTC/ccLogout.png)" onmouseover="this.style.backgroundImage=\'url(https://www.alumniconnections.com/olc/images/CTC/ccLogoutHover.png)\';" onmouseout="this.style.backgroundImage=\'url(https://www.alumniconnections.com/olc/images/CTC/ccLogout.png)\';" href="https://cas.conncoll.edu/cas/logout/?service=http://www.conncoll.edu/alumni/"></a>');
					}
					else
					{
						document.write('<a href="https://cas.conncoll.edu/cas/login?service=https://www.alumniconnections.com/olc/membersonly/CTC/mypage.jsp"></a>');
					}
				</script>
			</div>
		</div>
		<!-- End of HeaderTop -->
		<a href="http://www.conncoll.edu/index.htm"><img src="https://www.alumniconnections.com/olc/images/CTC/ccLogo.png" alt="Connecticut College Logo" class="logo"></a>
		<form name="seek1" method="GET" class="ccSearchForm" action="http://www.conncoll.edu:8765/query.html">

			<fieldset>
			<input type=hidden name=seek1SetCols value="true">
			<input type=hidden name=col value="public">
			<input type=hidden name=ht value="0">
			<input type=hidden name=qp value="">
			<input type=hidden name=qs value="">
			<input type=hidden name=qc value="">
			<input type=hidden name=pw value="100%">
			<input type=hidden name=ws value="0">

			<input type=hidden name=la value="">
			<input type=hidden name=qm value="0">
			<input type=hidden name=st value="1">
			<input type=hidden name=nh value="10">
			<input type=hidden name=lk value="1">
			<input type=hidden name=rf value="0">
			<input type=hidden name=oq value="">
			<input type=hidden name=rq value="0">
			<input type=hidden name=si value="1">

			<div class="ccSearchBox">
				<input type="text" name="qt" class="adSearch" value="Search connecticutcollege.edu" onfocus="if(this.value == 'Search connecticutcollege.edu'){this.value = '';}" />
			</div>
			<input type="submit" class="adSubmit" value="" />
			</fieldset>
		</form>
		<ul class="adTopNav">
			<li class="first"><a href="http://www.conncoll.edu/about/index.htm">At a Glance</a></li>

			<li><a href="http://www.conncoll.edu/academics/index.htm">Academics</a></li>
			<li><a href="http://www.conncoll.edu/admission/index.htm">Admission</a></li>
			<li><a href="http://www.conncoll.edu/campuslife/index.htm">Campus Life</a></li>
			<li><a href="http://www.conncoll.edu/centers/index.htm">Academic Centers</a></li>
			<li><a href="http://www.conncoll.edu/artsculture/index.htm">Arts &amp; Culture</a></li>

			<li><a href="http://www.conncoll.edu/sciences/index.htm">Sciences</a></li>
			<li><a href="http://www.conncoll.edu/athletics/">Athletics</a></li>
			<li><a href="http://makeagift.conncoll.edu/">Make A Gift</a></li>
		</ul>
	</div>
</div>
<div id="content">
	<div class="wrapper">

		<!--*****BEGIN MAIN CONTENT*****-->
		<div class="mainContent">
			<!--*****BEGIN LEFT COLUMN*****-->
			<div class="ccLeftNav">
				<!--*****BEGIN COTEXTUAL NAVIGATION*****-->
				<ul>
					<li><a href="http://www.conncoll.edu/alumni ">Alumni Home</a></li>
					<li><a href="https://www.alumniconnections.com/olc/membersonly/CTC/old/directory.cgi?FNC=basicsearch">Find Alumni</a></li>

					<li><a href="https://www.alumniconnections.com/olc/membersonly/CTC/mypage.jsp">My Profile</a></li>
					<li><a href="http://www.alumniconnections.com/olc/membersonly/CTC/hsearch/showSearch.jsp">Keyword Search</a></li>
					<li><a href="http://www.alumniconnections.com/olc/pub/CTC/ccservices.html">Career Center</a></li>
					<li><a href="http://www.conncoll.edu/alumni/12056.htm">Networking</a></li>
					<li><a href="http://www.conncoll.edu/alumni/12158.htm">Volunteer</a></li>
					<li><a href="http://www.alumniconnections.com/olc/pub/CTC/geventcal/showListView.jsp">Alumni Calendar</a></li>

					<li><a href="http://www.conncoll.edu/alumni/12057.htm ">Programs &amp; Events</a></li>
					<li><a href="http://www.alumniconnections.com/olc/membersonly/CTC/classnotes/classnotes.cgi">Class Notes</a></li>
					<li><a href="http://www.conncoll.edu/alumni/12059.htm">Alumni Association</a></li>
					<li><a href="http://www.conncoll.edu/alumni/12450.htm">Online Community Help</a></li>
					<li><a href="http://www.conncoll.edu/alumni/12052.htm">Support the College</a></li>

				</ul>
				<!--*****END CONTEXTUAL NAVIATION*****-->
			</div>
			<!--*****END LEFT COLUMN*****-->
			<!--*****BEGIN MAIN COLUMN*****-->
			<div class="ccMain">
				<!--*****BEGIN EXPLICIT/RANDOM BANNER*****-->
				<img class="banner" alt="" src="https://www.alumniconnections.com/olc/images/CTC/Science_bannerConstructionUpdates.jpg" id="mainBanner" style="display:none;" />
				<script language="javascript" type="text/javascript">
					var objBanner = document.getElementById("mainBanner");
					var arrImages = ["https://www.alumniconnections.com/olc/images/CTC/Science_bannerConstructionUpdates.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerObservatories.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerFacilities.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerStats.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerRotating3.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerRotating10.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_BannerCompSci.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerRotating6.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerBioLabs.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerZimmer.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerRotating7.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerNews.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerFacultyChemistry.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerEnv.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerPhysicsGeoAsto.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerNLHall.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerRotating.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_banner42.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerGrossel.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerScienceStudents.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerBioBotEnvSci.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerGreenhouse.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerOlin.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerChemLabs.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerRotating14.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerHammond.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerSiver.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerRotating11.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerRotating12.jpg",
													 "https://www.alumniconnections.com/olc/images/CTC/Science_bannerMonce.jpg"];
					var intCount = arrImages.length;
					var randomnumber=Math.floor(Math.random()*intCount);
					objBanner.src = arrImages[randomnumber];
				</script>

				<!--*****END RANDOM BANNER*****-->
				<!--*****BEGIN MAIN COLUMN - LEFT*****-->
				<div class="ccMainLeft">
					<!--*****BEGIN NARROW RANDOM BANNER****-->
					<!--*****END NARROW RANDOM BANNER*****-->
					<!--*****BEGIN BREADCRUMBS*****
					<ul class="breadcrumb" >
						<li class="last"><a href="http://www.conncoll.edu/sciences/index.htm">Sciences</a></li>
					</ul>
					*****END BREADCRUMBS*****-->


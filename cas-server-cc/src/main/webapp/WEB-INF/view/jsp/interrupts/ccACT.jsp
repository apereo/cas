<jsp:directive.include file="includes/Top.jsp" />
 <div class="info">
<h1>Email Activation</h1>
Your email address is: <a href="mailto://${userName}@conncoll.edu" style="font-weight:bold">${userName}@conncoll.edu</a><br />
<c:choose>
<c:when test='${fn:indexOf(NickName,".") > 0}'>
	People may also email you at <a href="mailto://${NickName}@conncoll.edu">${NickName}@conncoll.edu</a>
</c:when>
<c:otherwise>
	People may also email you at <a href="mailto://${firstname}.${lastname}@conncoll.edu">${firstname}.${lastname}@conncoll.edu</a>
</c:otherwise>
</c:choose>
Both addresses deliver mail to the same inbox. Most people prefer to give out their first.last@conncoll.edu address.<br />
To access your email go to <a href="http://mail.conncoll.edu" target="_blank">http://mail.conncoll.edu</a><br />
Type in your user ID: <strong>${userName}</strong><br />
You will need to type your new password again and accept Google’s terms of service the first time you access your e-mail. <br />
If you need assistance try the help web site at http://help.conncoll.edu. If you need further assistance please call the help desk at 1-860-439-HELP(4357)<br />

<h1>Thank you for Registering for CamelWeb, the Connecticut College Intranet!</h1>
The CamelWeb Home Page displays college news, important announcements, links to useful CameWeb sites and the campus calendar. Since much of this information changes often, it is a good idea to set it as your browser's home page. <br />
 
CamelWeb Quick Reference Guides for Faculty, Staff, and Students are available here:
<a href="http://www.conncoll.edu/computing/cwquickreference.htm" target="_blank">http://www.conncoll.edu/computing/cwquickreference.htm</a><br />
Please take a moment to read the guide most appropriate for you. It will familiarize you with the college intranet so that you can take advantage of all it has to offer.<br />

<p></p>
</div>
<jsp:directive.include file="includes/Bottom.jsp" />
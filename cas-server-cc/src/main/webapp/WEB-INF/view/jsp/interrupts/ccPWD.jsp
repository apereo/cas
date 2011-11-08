<jsp:directive.include file="includes/Top.jsp" />
<c:set var="hasForm" value="1" scope="page" />
<div class="info">
<h1>How to change your password</h1> 
<strong>Your current password is no longer valid and must be change before you can access Connecticut College resources.</strong>

<div style="display: none; background: #f00;" id="MainErrorHead"><strong>There was an error with your form. Please fix all fields as noted below in pink.</strong> </div>
<p>The password cannot contain all or part of your user account name and it must be <b>at least</b> eight characters in length. The password also <b>may not contain any part of your login id</b>. In addition it needs to contain characters from three of the following categories:</p>
<p>English uppercase characters (A through Z)<br />

English lower case characters (a through z)<br />
Numbers (0 through 9)<br />
Non-alphabetic characters (for example, ! $,#, %).</p>
<p>If you are unable to log in to your email account after changing your password, contact the computer Help Desk at x4357 </p>
<p>Below are some examples of passwords that would follow the necessary criteria:<br />
&amp;Ez2do Suce$$ful 2S!ncere Etc&amp;etc Came12oo4</p>

<p>Passwords will now need to be changed every 180 days. Please choose a password that meets the above requirements but is easy enough to remember that you do not have to write it down. The three previous passwords cannot be used. (If you do feel the need to write your password down, please store it in a secure location!)<br />
<em>Do not use the example passwords above.</em></p>
 
<script type="text/javascript"> 
 
if (document.location.protocol != "https:") { 
	document.location.href = "https://"+document.domain +location.pathname; 
} 
var ccPopUp=0; 
var ccPassMin=8;
var ErrorColor = "#FFDAD9"; 
var ccFuncOnInvalid = true; 
ccHTMLHead = '<strong>'; 
ccHTMLFoot = '</strong>'; 
function InLineValid(){ 
	bvalid = true; 
	var p1 = document.getElementById("field01"); 
	var p2 = document.getElementById("field01"); 
	var pe = document.getElementById("field01Error"); 
	if (p1.value != p2.value) { 
		bvalid = false; 
		pe.innerHTML = pe.innerHTML + '<strong> New password and confirm new password must match.</strong>'; 
		pe.style.display=''; 
		p1.style.backgroundColor=ErrorColor; 
		p2.style.backgroundColor=ErrorColor; 
		document.getElementById("MainErrorHead").style.display=''; 
		document.getElementById("MainErrorFoot").style.display=''; 
	}  
	return bvalid; 
} 
</script> 
<style type="text/css"> 
input { 
	color: #01376E; 
	border: 1px solid #2F5989; 
} 

select { 
	color: #01376E; 
} 
</style> 
<form:form commandName="${commandName}" htmlEscape="true" method="post">
    <form:errors path="*" cssClass="errors" id="status" element="div" />
    <strong>Choose a password:</strong><br />
    <c:choose>
    	<c:when test='${fn:length(ErrorMsg)>3}'>
        	<div id="MainErrorHead" style="background:#F00"> 
				<strong>There was an error with your form. Please fix all fields as noted above in pink.</strong> 
			</div>
        	<div id="field01Error" style="background:#FFDAD9">
            	${ErrorMsg}
            </div>
        </c:when>
        <c:otherwise>
        	<div id="MainErrorHead" style="display:none;background:#F00"> 
				<strong>There was an error with your form. Please fix all fields as noted above in pink.</strong> 
			</div>
        	<div id="field01Error" style="display:none;background:#FFDAD9"></div>        
        </c:otherwise>
    </c:choose> 
    <input type="password" Class="required" size="25" tabindex="1" id="field01" name="fields[1]" ccvalid="password"  /><br /><br />
    <strong>Re-enter your password:</strong><br />
    <input type="password" Class="required" size="25" tabindex="1" id="field01" name="fields[2]" ccvalid="password"  /><br /><br />				
    <c:choose>
    	<c:when test='${fn:length(ErrorMsg)>3}'>
        	<div id="MainErrorFoot" style="background:#F00"> 
				<strong>There was an error with your form. Please fix all fields as noted above in pink.</strong> 
			</div>
        </c:when>
        <c:otherwise>
        	<div id="MainErrorFoot" style="display:none;background:#F00"> 
				<strong>There was an error with your form. Please fix all fields as noted above in pink.</strong> 
			</div>
        	<div id="field01Error" style="display:none;background:#FFDAD9"></div>        
        </c:otherwise>
    </c:choose>     
	<input type="hidden" name="lt" value="${loginTicket}" />
	<input type="hidden" name="execution" value="${flowExecutionKey}" />
	<input type="hidden" name="_eventId" value="submit" />
    <div align="center"><input type="submit" value="Continue" id="btnSubmit" /></div>
    <strong>
    <em>Important to Note: </em><br /><br />
    If this is your first login, you will need to type your new password again and accept Google’s terms of service the first time you access your e-mail.<br /><br />
    Changing your password on this page will change the password you use to log in to the college network and your email account. Mac users will need to change the password stored on their computer for email, wireless access to the college network, VPN, and address book.  
	</strong>
</form:form>
<script src="https://aspen.conncoll.edu/scripts/ccValidator.js"></script
><p></p>
</div>
<jsp:directive.include file="includes/Bottom.jsp" />
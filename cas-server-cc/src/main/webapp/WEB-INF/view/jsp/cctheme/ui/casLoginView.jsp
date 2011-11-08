<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<jsp:directive.include file="includes/top.jsp" />
			<form:form method="post" id="fm1" cssClass="fm-v clearfix" commandName="${commandName}" htmlEscape="true">
			    <form:errors path="*" cssClass="errors" id="status" element="div" />
                <table width="360" cellpadding="1" style="margin-left:100px;">
                    <tr>
                        <td width="140"></td>
                        <td width="220"></td>
                    <tr>
                        <td colspan="2" align="right" style="color:#003366">
                            <img src="images/CollegeName.jpg" width="285" height="24" vspace="4" /><br />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center">
                            <h3><i>Logging into this page gives you<br />access to e-mail, CamelWeb and Moodle.</i></h3>
                        </td>    	
                    </tr>
                    <tr height="43">
                        <td align="right" style="color:#003366" valign="middle" height="43"><strong>USER ID:</strong></td>
                        <td class="input">
                            <form:input cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="false" htmlEscape="true" />
                        </td>
                    </tr>
                    <tr>
                        <td></td>
                        <td  style="font-size:small">Enter your user ID only. Example: jsmith<br />  
Do not use jsmith@conncoll.edu or joe.smith@conncoll.edu or joe.smith</td>
                    </tr>
                    <tr height="43">
                        <td align="right" style="color:#003366" valign="middle" height="43"><strong>PASSWORD:</strong></td>
                        <td class="input">
                            <form:password cssClass="required" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
                        </td>
                    </tr>
                    <tr>
                        <td></td>
                        <td align="right">
						<input type="hidden" name="lt" value="${loginTicket}" />
						<input type="hidden" name="execution" value="${flowExecutionKey}" />
						<input type="hidden" name="_eventId" value="submit" />

                    <input type="image" src="images/Login.jpg" width="163" height="45" /></td>
                    </tr>
                    <tr>
                        <td></td>
                        <td colspan="1" align="center" style="font-size:small">
                        </td>
                    </tr>
                    <tr>
                        <td></td>
                        <td align="center" style="font-size:small"><a href="http://www.conncoll.edu">Connecticut College home page</a></td>
                    </tr>
                </table>
        	</form:form>
<script type="text/javascript">
	var UserObj = document.getElementById('username');
	var PassObj = document.getElementById('password');
	if (UserObj.value == ''){
		UserObj.focus();
	} else {
		PassObj.focus();
	}
</script>
<jsp:directive.include file="includes/bottom.jsp" />

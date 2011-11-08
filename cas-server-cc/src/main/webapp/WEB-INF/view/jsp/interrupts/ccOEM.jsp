<jsp:directive.include file="includes/Top.jsp" />
<h2>Account Activation Notice</h2>
<c:choose>
	<c:when test='${fn:indexOf(oemail,"@") > 0}'>
        A confirmation email has been sent to your <b>${oemail}</b> email account.<br />
        This email is for your records and you will not need to respond to it.
    </c:when>
    <c:otherwise>
    	Your account was flagged to receive an email notification when activated, however you have no off-campus email stored in our system.<br />
    </c:otherwise>
</c:choose>

<jsp:directive.include file="includes/Bottom.jsp" />
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--
  Created by IntelliJ IDEA.
  User: ruaa
  Date: 15. 5. 29.
  Time: 오후 4:07
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Login</title>
  <link rel="stylesheet" href="/css/login.css"/>
  <meta name="viewport" content="width=device-width, user-scalable=no">
</head>
<body>
<div id="wrap">

    <div id="content" class="seciton_log">
      <div class="group_main">
        <div class="fl">
          <img src="https://static.nhnent.com/static/site/whatsup/resources_ent/img/login_logo_v2.gif" alt="NHN Entertainment Terminal">
        </div>
        <div class="rgt">
          <%--<fieldset>--%>
            <%--<form id="thisform" name="LoginForm" action="/login" method="post">--%>
            <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true">
              <form:errors path="*" id="msg" cssClass="errors" element="div" htmlEscape="false" />

              <%--<legend class="blind">Login Information</legend>--%>
              <div class="info_area">
                <div class="input_login_info">
                  <div>
                    <label for="username"><spring:message code="screen.welcome.label.netid" /></label>
                    <c:choose>
                      <c:when test="${not empty sessionScope.openIdLocalId}">
                        <strong><c:out value="${sessionScope.openIdLocalId}" /></strong>
                        <input type="hidden" id="username" name="username" value="<c:out value="${sessionScope.openIdLocalId}" />" />
                      </c:when>
                      <c:otherwise>
                        <spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
                        <form:input cssClass="required inpt_type" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="off" htmlEscape="true" autofocus="autofocus" />
                      </c:otherwise>
                    </c:choose>
                  </div>
                  <div>
                    <label for="password"><spring:message code="screen.welcome.label.password" /></label>
                    <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
                    <form:password cssClass="required inpt_type" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
                    <span id="capslock-on" style="display:none;"><p><img src="images/warning.png" valign="top"> <spring:message code="screen.capslock.on" /></p></span>
                  </div>
                </div>

                <%--<input onclick="submitLogin(); return false;" type="image" src="http://static.nhnent.com/static/site/whatsup/resources_ent/img/btn_login.gif" alt="LOGIN" class="btn_login">--%>
                <%--<input class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="6" type="submit" />--%>
                <%--<button type="submit" name="submit" accesskey="l" tabindex="6" style="border:0;">--%>
                  <%--<img src="http://static.nhnent.com/static/site/whatsup/resources_ent/img/btn_login.gif" />--%>
                <%--</button>--%>
                <button type="submit" name="submit" class="btn_red" accesskey="l" tabindex="6" style="border:0;">LOGIN</button>
                <input type="hidden" name="lt" value="${loginTicket}" />
                <input type="hidden" name="execution" value="${flowExecutionKey}" />
                <input type="hidden" name="_eventId" value="submit" />
                <input type="hidden" name="rememberMe" value="rememberMe" />
              </div>
              <%--<div class="more_area">--%>
                <%--<input type="checkbox" id="remember_id" class="chck_type">--%>
                <%--<label for="remember_id" class="txt_remb">Remember User ID</label>--%>
                <%--<div class="area_help">--%>
                  <%--<a href="#" id="id_btn_forgetWindow" class="txt_forget">Forget User Password?</a>--%>
                  <%--<a href="#" onClick="openHelpDesk();" class="txt_helpdesk">Help Desk</a>--%>
                <%--</div>--%>
              <%--</div>--%>
            </form:form>
          <%--</fieldset>--%>
        </div>
      </div>
    </div>

</div>
<script type="text/javascript">
  function submitLogin() {
    document.forms.LoginForm.submit();
  }
</script>
</body>
</html>

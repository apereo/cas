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
  <style type="text/css">
    /* Common */
    body,p,h1,h2,h3,h4,h5,h6,menu,ul,ol,li,dl,dt,dd,table,th,td,form,fieldset,legend,input,textarea,button,select{margin:0;padding:0;-webkit-text-size-adjust:none}
    body,select,input{font-family:'나눔고딕',NanumGothic,'돋움',Dotum,AppleGothic,sans-serif;font-size:11px;color:#333}
    img,fieldset{border:0}
    a{text-decoration:none}
    a:hover,a:active,a:focus{text-decoration:underline}
    input[type=image],input[type=text],input[type=password]{-webkit-appearance:none;-webkit-border-radius:0;resize:none}
    .blind{overflow:hidden;position:absolute;width:0;height:0;font-size:0;line-height:0}

    /* login */
    #wrap{width:715px;margin:0 auto}
    .seciton_log{position:relative;padding:211px 0 229px}
    .seciton_log .group_main{height:146px;padding-top:51px;border:2px solid #d9d9d9}
    .seciton_log .group_main .fl{float:left;width:325px;height:94px;border-right:1px solid #e0e0e0}
    .seciton_log .group_main .fl h1{padding-top:5px;text-align:center}

    .seciton_log .rgt{float:left;padding-left:46px}
    .seciton_log .rgt .btn_login{margin:1px 0 0 5px}
    .seciton_log .info_area{clear:both}
    .seciton_log .info_area dl{float:left;width:214px;zoom:1;padding-top:1px}
    .seciton_log .info_area dt{clear:left;float:left;width:74px;height:8px}
    .seciton_log .info_area dt label{overflow:hidden;display:block;margin:9px 0 9px 0;}
    .seciton_log .info_area dt label.txt_id{width:10px}
    .seciton_log .info_area dt label.txt_pw{width:56px;background-position:-20px 0}
    .seciton_log .info_area dd{float:left;margin-bottom:4px}
    .seciton_log .info_area dd .inpt_type{width:128px;padding:5px 5px;border:1px solid #e1e1e1;line-height:14px}

    .seciton_log .more_area{clear:both;margin:6px 0 0 75px}
    .seciton_log .more_area .txt_remb,.seciton_log .more_area .txt_forget, .more_area .txt_helpdesk{overflow:hidden;display:inline-block;background:url(http://staticdev.nhnent.com/static/site/whatsup/resources_ent/img/sp_login.png?ver=20150118) no-repeat;line-height:999px;vertical-align:middle}
    .seciton_log .more_area .txt_remb{display:inline-block;width:90px;height:7px;margin-left:2px;background-position:0 -20px}
    .seciton_log .more_area .area_help{margin-top:6px}
    .seciton_log .more_area .area_help .txt_forget{width:121px;height:11px;background-position:0 -38px}
    .seciton_log .more_area .area_help .txt_helpdesk{width:60px;height:11px;margin-left:8px;background-position:0 -58px}
    .seciton_log .more_area .chck_type{vertical-align:middle}

    .seciton_log .opt_area{clear:both;margin:10px 6px 0 0;text-align:right}
    .seciton_log .opt_area .txt_lan{overflow:hidden;display:inline-block;width:50px;height:7px;margin:2px 7px 0 0;background:url(http://staticdev.nhnent.com/static/site/whatsup/resources_ent/img/sp_login.png?ver=20150118) -90px 0 no-repeat;line-height:999px;vertical-align:middle}
    .seciton_log .opt_area select{height:18px;vertical-align:middle}

    #footer{padding-bottom:58px;line-height:19px;color:#444;text-align:center}
    #footer a{color:#444}
    #footer .corp{font-weight:bold}
  </style>
</head>
<body>
<div id="wrap">
  <div id="container">
    <div id="content" class="seciton_log">
      <div class="group_main">
        <div class="fl">
          <h1><img src="http://static.nhnent.com/static/site/whatsup/resources_ent/img/login_logo_v2.gif" alt="NHN Entertainment Terminal"></h1>
        </div>
        <div class="rgt">
          <fieldset>
            <%--<form id="thisform" name="LoginForm" action="/login" method="post">--%>
            <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true">
              <form:errors path="*" id="msg" cssClass="errors" element="div" htmlEscape="false" />

              <legend class="blind">Login Information</legend>
              <div class="info_area">
                <dl>
                  <%--<dt><label for="user_id" class="txt_id">ID</label></dt>--%>
                  <%--<dd><input id="user_id" name="USER" onkeypress="return executeCustomEnter(event);" style="ime-mode:disabled;" class="inpt_type" type="text" value=""/></dd>--%>
                  <dt><label for="username"><spring:message code="screen.welcome.label.netid" /></label></dt>
                  <dd>
                  <c:choose>
                    <c:when test="${not empty sessionScope.openIdLocalId}">
                      <strong><c:out value="${sessionScope.openIdLocalId}" /></strong>
                      <input type="hidden" id="username" name="username" value="<c:out value="${sessionScope.openIdLocalId}" />" />
                    </c:when>
                    <c:otherwise>
                      <spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
                      <form:input cssClass="required inpt_type" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="off" htmlEscape="true" />
                    </c:otherwise>
                  </c:choose>
                  </dd>
                  <%--<dt><label for="user_pw" class="txt_pw">PASSWORD</label></dt>--%>
                  <%--<dd><input id="user_pw" name="PASSWORD" onkeypress="return executeCustomEnter(event);" class="inpt_type" type="password" value=""/></dd>--%>
                  <dt><label for="password"><spring:message code="screen.welcome.label.password" /></label></dt>
                    <%--
                    NOTE: Certain browsers will offer the option of caching passwords for a user.  There is a non-standard attribute,
                    "autocomplete" that when set to "off" will tell certain browsers not to prompt to cache credentials.  For more
                    information, see the following web page:
                    http://www.technofundo.com/tech/web/ie_autocomplete.html
                    --%>
                  <dd>
                    <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
                    <form:password cssClass="required inpt_type" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
                    <span id="capslock-on" style="display:none;"><p><img src="images/warning.png" valign="top"> <spring:message code="screen.capslock.on" /></p></span>
                  </dd>
                </dl>
                <%--<input onclick="submitLogin(); return false;" type="image" src="http://static.nhnent.com/static/site/whatsup/resources_ent/img/btn_login.gif" alt="LOGIN" class="btn_login">--%>
                <%--<input class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="6" type="submit" />--%>
                <button type="submit" name="submit" accesskey="l" tabindex="6" style="border:0;">
                  <img src="http://static.nhnent.com/static/site/whatsup/resources_ent/img/btn_login.gif" />
                </button>
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
          </fieldset>
        </div>
      </div>
    </div>
  </div>
  <div id="footer">
    <p>&nbsp;</p>
  </div>
</div>
<script type="text/javascript">
  function submitLogin() {
    document.forms.LoginForm.submit();
  }
</script>
</body>
</html>

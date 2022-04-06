<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title><spring:message code="root.title" text="Shibboleth IdP" /></title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/css/main.css">
  </head>

  <body>
    <div class="wrapper">
      <div class="container">
        <header>
          <img src="<%= request.getContextPath() %><spring:message code="idp.logo" />" alt="<spring:message code="idp.logo.alt-text" text="logo" />">
        </header>
    
        <div class="content">
          <h2><spring:message code="shibCasAuthn.errorMessage" text="Sorry, it looks like there is a problem finding your session. This can happen if you waited too long on the login page, or if you were redirected to a different server that issued the original request. This error usually goes away if you try accessing your desired application again." /></h2>
        </div>
      </div>

      <footer>
        <div class="container container-footer">
          <p class="footer-text"><spring:message code="root.footer" text="Insert your footer text here." /></p>
        </div>
      </footer>
    </div>

  </body>
</html>

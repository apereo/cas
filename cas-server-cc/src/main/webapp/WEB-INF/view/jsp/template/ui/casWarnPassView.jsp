<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<spring:theme code="theme.casWarnPassView.url" text="/WEB-INF/view/jsp/default/ui/casWarnPassView.jsp" var="myview" />
<jsp:include page="${myview}" />
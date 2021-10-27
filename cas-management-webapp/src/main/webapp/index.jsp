<%@ page language="java"  session="false" %>
<%
final String url = request.getContextPath() + "/manage.html";
response.sendRedirect(response.encodeURL(url));%>

<%@ page import="java.util.Set, java.util.Map, java.util.Iterator" %><%
    Map parameters = (Map)request.getAttribute("parameters");
    Iterator iterator = parameters.keySet().iterator();
    while (iterator.hasNext()) {
        String key = (String)iterator.next();
        String parameter = (String)parameters.get(key);
        out.print(key+":"+parameter+"\n");
    }
%>
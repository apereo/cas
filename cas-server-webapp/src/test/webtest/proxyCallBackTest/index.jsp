<%
if( request.getParameter("pgtId") != null ) {
	System.out.println("Set PGT : #" + request.getParameter("pgtId") + "#");
	application.setAttribute("pgtId",request.getParameter("pgtId")); 
} else {
	System.out.println("Get PGT : #" + application.getAttribute("pgtId") + "#");
	out.println("PGT: #" + application.getAttribute("pgtId") + "#");	
}
%>
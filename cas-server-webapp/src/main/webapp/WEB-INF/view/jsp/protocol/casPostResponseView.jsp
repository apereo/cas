<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@ page language="java"  session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
 <body onload="document.acsForm.submit();">
   <form name="acsForm" action="<c:out value="${originalUrl}" escapeXml="true" />" method="post">
     <div style="display: none">
       <c:forEach items="${parameters}" var="entry">
         <textarea rows=10 cols=80 name="${entry.key}"><c:out value="${entry.value}" escapeXml="true" /></textarea>
       </c:forEach>
     </div>
     <noscript>
       <p>You are being redirected to <c:out value="${originalUrl}" escapeXml="true" />. Please click &quot;Continue&quot; to continue your login.</p>
       <p><input type="submit" value="Continue" /></p>
     </noscript>
   </form>
 </body>
</html>
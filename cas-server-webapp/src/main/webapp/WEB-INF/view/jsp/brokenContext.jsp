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
<jsp:directive.include file="default/ui/includes/top.jsp" />
	<div id="welcome">
			<h2>CAS is Unavailable</h2>

			<p>
			  There was a fatal error initializing the CAS application context.  This is almost always because of an error in the Spring bean configuration files.
			  Are the files valid XML?  Do the beans they refer to all exist?<br /><br />
			  Before placing CAS in production, you should change this page to present a UI appropriate for the case where the CAS
			  web application is fundamentally broken.  Perhaps "Sorry, CAS is currently unavailable." with some links to your user support information.
			</p>
		
			<c:if test="${not empty applicationScope.exceptionCaughtByServlet and empty applicationScope.exceptionCaughtByListener}">
			<p>
			  The Throwable representing the fatal error has been logged by the <em>SafeDispatcherServlet</em>
			  via Commons Logging, via ServletContext logging, and to System.err.
			</p>
			</c:if>
			
			<c:if test="${empty applicationScope.exceptionCaughtByServlet and not empty applicationScope.exceptionCaughtByListener}">
			<p>
			  The Throwable representing the fatal error has been logged by the SafeContextLoaderListener
			  via Commons Logging, via ServletContext logging, and to System.err.
			</p>
			</c:if>
			
			<!-- Render information about the throwables themselves -->
			
			<c:if test="${not empty applicationScope.exceptionCaughtByListener}">
			<p>
			  The Throwable encountered at context listener initialization was: <br/> <br/>
			  <c:out value="${applicationScope.exceptionCaughtByListener}"/>
			</p>
			</c:if>
			
			<c:if test="${not empty applicationScope.exceptionCaughtByServlet}">
			<p>
			  The Throwable encountered at dispatcher servlet initialization was: <br/> <br/>
			  <c:out value="${applicationScope.exceptionCaughtByServlet}"/>
			</p>
			</c:if>
	</div>
<jsp:directive.include file="default/ui/includes/bottom.jsp" />


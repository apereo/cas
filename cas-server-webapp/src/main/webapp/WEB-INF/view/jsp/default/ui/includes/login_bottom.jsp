<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
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
			<%-- <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
			</div> <!-- END #content -->
			<footer>
			  <div id="copyright" class="container">
			    <p><spring:message code="copyright" /></p>
			        <p>Powered by <a href="http://www.apereo.org/cas">
			            WIS Central Authentication Service <%=org.jasig.cas.CasVersion.getVersion()%></a>
			            <%=org.jasig.cas.CasVersion.getDateTime()%></p>
			  </div>
			</footer>
		</div> <!-- END #container --> --%>
		
			<!--------- Footer ------------>
			<div id="footer">
				<div class="loginfooter">
					<ul class="menu list-inline">
						<li>Copyright @ Ziontech. All rights reserved.<%-- <%= nls.COPYRIGHT %> --%></li><span>|</span>
						<li><a href="javascript:;;">About</a></li><span>|</span>
						<li><a href="javascript:;;">Privacy</a></li><span>|</span>
						<li><a href="javascript:;;">Terms</a></li><span>|</span>
						<li><a href="javascript:;;">Help</a></li>
						<li class="pull-right">						
							<a href="javascript:;;" class="languagemenu" data-toggle="dropdown"><span class="fa fa-globe globeicon m-r-5"></span>US (English)</a>
							<ul class="tile list-unstyled">
								<li><a href="javascript:;;">US(English)</a></li>
								<li><a href="javascript:;;">GB(English)</a></li>
								<li><a href="javascript:;;">FR(Francis)</a></li>
							</ul>
						</li>
					</ul>
				</div>
			</div>
		</div> <!-- end Content -->
	</body>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/headjs/1.0.3/head.min.js"></script>
</html>
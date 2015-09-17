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
<%@include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp"%>

<script type="text/javascript">
    function jqueryReady() {

    }
</script>

<div>
    <div>
        <h1>Log Configuration Dashboard</h1>
        <p>
        <div id="msg" class="info">
            CAS logging configuration is loaded from ${logConfigurationFile}
        </div>

        <div id="login">
            <div><br/></div>
            <input class="btn-submit" type="button" onclick="location.reload();" value="Refresh">
        </div>

    </div>
</div>


<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>

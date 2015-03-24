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

    function parseJsonPayload() {

        var object = $.parseJSON('${activeSsoSessions}');
        if (object != undefined) {
            var activeSsoSessions = object.activeSsoSessions;
            if (activeSsoSessions.length == 0)	 {
                showInfo("No SSO Sessions are available at this point.");
            } else {
                $("#jsonContent").empty();
                for (var i = activeSsoSessions.length - 1; i >= 0; i--) {
                    var ssoSession = activeSsoSessions[i];
                    $("#jsonContent").append("<div class='accordion-group'>"
                    + "<a class='accordion-toggle' "
                    + "href='#" + ssoSession.authenticated_principal + i + "' "
                    + "data-toggle='collapse'>Principal: " + ssoSession.authenticated_principal
                    + "</a></div>");


                    $("#jsonContent").append("<div id='" + ssoSession.authenticated_principal + i + "' "
                    + "class='accordion-body collapse'> ");

                    $("#jsonContent").append("<div class='accordion-inner'>");

                    $("#jsonContent").append("<table border=3px class='table table-striped table-condensed'>");

                    $("#jsonContent").append("<thead><tr>");
                    $("#jsonContent").append("<th>Authentication Date</th><th>Number of uses for this SSO session</th>");
                    $("#jsonContent").append("</tr></thead>");

                    $("#jsonContent").append("<tbody><tr>");
                    $("#jsonContent").append("<td>"
                    + ssoSession.authentication_date
                    + "</td>"
                    + "<td>"
                    + ssoSession.number_of_uses
                    + "</td>");

                    $("#jsonContent").append("</tr></tbody>");
                    $("#jsonContent").append("</table></div></div>");
                };
            }
        } else {
            showError("Did not receive the SSO Sessions JSON payload from the CAS server.");
        }
    }

    function showError(msg) {
        $("#msg").removeClass();
        $("#msg").addClass("errors");
        $("#msg").text(msg);
        $("#msg").show();
    }

    function showInfo(msg) {
        $("#msg").removeClass();
        $("#msg").addClass("info");
        $("#msg").text(msg);
        $("#msg").show();
    }

    function jqueryReady() {
        parseJsonPayload();
    }
</script>

<div>
    <div>
        <div id="msg" style="display:none"></div>
        <div id="login">
            <input class="btn-submit" type="button" onclick="location.reload();" value="Refresh">
        </div>

        <div id="jsonContent"></div>

    </div>
</div>


<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>

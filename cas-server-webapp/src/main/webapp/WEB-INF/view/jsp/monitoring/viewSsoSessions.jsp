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

<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/flick/jquery-ui.css">
    

    
<script type="text/javascript">

    function parseJsonPayload() {

        var object = $.parseJSON('${activeSsoSessions}');
        if (object != undefined) {
            var activeSsoSessions = object.activeSsoSessions;
            if (activeSsoSessions == undefined || activeSsoSessions.length == 0)	 {
                showInfo("No SSO Sessions are available at this point.");
            } else {
                $("#jsonContent").empty();
                
                for (var i = activeSsoSessions.length - 1; i >= 0; i--) {
                    var sso = activeSsoSessions[i];                   
                    
                    $("#jsonContent").append("<h3><div class='row'>"
                    + "<div class='col-xs-3'><span>" + sso.authenticated_principal + "</span></div>"
                    + "<div class='col-xs-2'><span>" + sso.authenticated_principal + "</span></div>"
                    + "<div class='col-xs-3'><span>" + sso.authentication_date + "</span></div>"
                    + "<div class='col-xs-2'><span>" + sso.number_of_uses + "</span></div>"
                    + "<div class='col-xs-2'><button class='btn btn-danger btn-xs' type='submit'>Kill</button></div>"
                    + "</div></h3>");

                    $("#jsonContent").append("<div>"
                    + "<table class='table table-striped table-hover'>"
                    //+ "<tr><th>Authentication Date:</th><th>Usage Count</th><th>Ticket Granting Ticket</th></tr>"
                    //+ "<tr><td>" + sso.authentication_date + "</td>"
                    //+ "<tr><td>" + sso.number_of_uses + "</td>"
                    + "<tr><td>Ticket Granting Ticket</td>"
                    + "<td>" + (sso.ticket_granting_ticket == undefined ? new Array(30).join("*") : sso.ticket_granting_ticket) + "</td></tr>"
                    + "<tr><td>Principal Attributes</td>"
                    + "<td>SAMPLE</td></tr>"
                    + "<tr><td>Proxy Ticket</td>"
                    + "<td>YES</td></tr>"
                    + "<tr><td>Ticket Granting Ticket Service</td>"
                    + "<td>SAMPLE</td></tr>"
                    + "</table>"
                    + "</div>");
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
        $("#jsonContent").accordion();
    }
</script>


<div>
       <h1>SSO Sessions Report</h1>

    <div id="container-stable">
       <div id="table-monitor-hdr" class="row">
            <div class="col-xs-3">&nbsp;</div>
            <div class="col-xs-2">User</div>
            <div class="col-xs-3">Authentication Date</div>
            <div class="col-xs-2">Usage Count</div>
            <div class="col-xs-2">Kill</div>
       </div>

       <div id="msg" style="display:none"></div>
        
       <div id="jsonContent"></div>
    </div>

       <div id="login">
            <div><br/></div>
            <input class="btn btn-success" type="button" onclick="location.reload();" value="Refresh">
       </div>
</div>



<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>

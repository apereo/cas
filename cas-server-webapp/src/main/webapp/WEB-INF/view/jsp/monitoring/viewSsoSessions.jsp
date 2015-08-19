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
 
  <!-- Twitter Bootstrap UI framework -->
  <link href="/cas/css/bootstrap.min.css" rel="stylesheet">  

  <!-- Google Fonts -->
  <link href='//fonts.googleapis.com/css?family=Lato:300,400,700' rel='stylesheet' type='text/css'>
  <link href='//fonts.googleapis.com/css?family=Open+Sans:400,700,800' rel='stylesheet' type='text/css'>   

  <link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
    
  <!-- Custom UI styles -->
  <link href="/cas/css/customcas.css" rel="stylesheet"> 
    
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
                    
                    $("#jsonContent").append("<h3><div class='row text-center'>"
                    + "<div class='col-xs-1'><span class='label label-primary'>Proxy</span></div>"
                    + "<div class='col-xs-3'><span>" + sso.authenticated_principal + "</span></div>"
                    + "<div class='col-xs-4'><span>" + sso.authentication_date + "</span></div>"
                    + "<div class='col-xs-2'><span>" + sso.number_of_uses + "</span></div>"
                    + "<div class='col-xs-2'><span class='glyphicon glyphicon-flash' aria-hidden='true'></span></div>"
                    + "</div></h3>");

                    $("#jsonContent").append("<div>"
                    + "<table class='table table-bordered table-striped table-hover'>"
                    + "<tr><td>Ticket Granting Ticket</td>"
                    + "<td>" + (sso.ticket_granting_ticket == undefined ? new Array(30).join("*") : sso.ticket_granting_ticket) + "</td></tr>"
                    + "<tr><td>Principal Attributes</td>"
                    + "<td>Insert Datatable here...</td></tr>"
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

<div id="cas-sessions">
    
    
        <!-- Main Header/Navigation
        <nav class="navbar navbar-default navbar-static-top" id="top-navbar" role="navigation">
            <div class="container">
                <span class="navbar-brand" href="#"><span class="glyphicon glyphicon-stats" aria-hidden="true"></span>
                    <span class="logo hidden-xs"><span class="heavy">SSO</span>Sessions Report</span></span>
            </div>
        </nav> -->
    
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4><span class="glyphicon glyphicon-stats" aria-hidden="true"></span> SSO Sessions Report</h4>
        </div>
        <div class="panel-body">
            <div>

            <div class="btn-group btn-group-sm pull-right" data-toggle="buttons">
              <label class="btn btn-primary active">
                <input type="radio" name="options" id="option1" autocomplete="off" checked> All
              </label>
              <label class="btn btn-primary">
                <input type="radio" name="options" id="option2" autocomplete="off"> Proxied
              </label>
              <label class="btn btn-primary">
                <input type="radio" name="options" id="option3" autocomplete="off"> Non-Proxied
              </label>
            </div>

            <button class="btn btn-sm btn-danger" type="button">Bulk Kill</button></div>
            
            <div id="container-stable">
                
               <div id="msg" style="display:none"></div>
                
               <div id="table-monitor-hdr" class="row">
                    <div class="col-xs-1">&nbsp;</div>
                    <div class="col-xs-3">User</div>
                    <div class="col-xs-4">Authentication Date</div>
                    <div class="col-xs-2">Usage Count</div>
                    <div class="col-xs-2">Kill</div>
               </div>

               <div id="jsonContent"></div>            
                
            </div>

            <div id="login">
                    <input class="btn btn-success btn-sm" type="button" onclick="location.reload();" value="Refresh">
            </div>
        </div>
    </div>
</div>

<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>

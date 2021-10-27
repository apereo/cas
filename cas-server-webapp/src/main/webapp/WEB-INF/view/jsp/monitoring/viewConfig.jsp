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


<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/cupertino/jquery-ui.css">
<link rel="stylesheet" href="//cdn.datatables.net/plug-ins/1.10.6/integration/jqueryui/dataTables.jqueryui.css" />

<style>
    #container {
        width:98%;
    }
</style>
<script type="text/javascript">

    function listBeanProperties(beanName,beanObject,table) {
        var bean = beanObject[beanName];

        for (var propertyName in bean) {
            if (bean.hasOwnProperty(propertyName)) {
                var propValue = bean[propertyName];
                if (typeof propValue == 'string') {
                    table.row.add( [beanName + "." + propertyName,propValue] ).draw();
                } else {
                    //bean has an object property
                    for (var propObj1 in propValue) {
                        if (propValue.hasOwnProperty(propObj1)) {
                            var propObj1Value = propValue[propObj1];

                            if (typeof propObj1Value == 'string') {
                                table.row.add( [beanName + "." + propertyName + "."
                                + propObj1,propObj1Value] ).draw();
                            } else {
                                //bean has an object property
                                for (var propObj2 in propObj1Value) {
                                    if (propObj1Value.hasOwnProperty(propObj2)) {
                                        var propObj2Value = propObj1Value[propObj2];
                                        table.row.add( [beanName + "." + propertyName + "."
                                        + propObj1 + "." + propObj2,propObj2Value] ).draw();
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    function parseJsonPayload() {
        var table = $("#jsonTable").DataTable();
        var objects = $.parseJSON('${jsonData}');

        for (var i = 0; i < objects.length; i++) {
            var bean = objects[i];
            for (var beanName in bean) {
                if (bean.hasOwnProperty(beanName)) {
                    table.row.add( [beanName,""] ).draw();
                    listBeanProperties(beanName, bean, table)
                }
            }
        }
    }

    function jqueryReady() {
        var scripts = [ "https://cdn.datatables.net/1.10.6/js/jquery.dataTables.min.js",
            "https://cdn.datatables.net/plug-ins/1.10.6/integration/jqueryui/dataTables.jqueryui.js"];

        head.ready(document, function() {
            head.load(scripts, function() {
                var table = $("#jsonTable").DataTable({
                    "columnDefs": [
                        { "width": "40%", "targets": 0 }
                    ],
                    "jQueryUI": true
                });
                parseJsonPayload();

                $("#jsonTable").show();
                $("#msg").hide();
            });
        });


    }
</script>


<div>
    <div>
        <h1>CAS Server Internal Configuration</h1>
        <p>
        <div id="msg" class="info">Please wait...</div>

        <div id="jsonContent">
            <table id="jsonTable" class="display" cellspacing="0" width="100%" style="display:none">
                <thead>
                <tr>
                    <th>Key</th>
                    <th>Value</th>
                </tr>
                </thead>

                <tbody>

                <c:forEach items="${properties}" var="p">
                    <tr>
                        <td>casProperty.${p.key}</td>
                        <td>${p.value}</td>
                    </tr>
                </c:forEach>

                </tbody>
            </table>
        </div>

        <div id="login">
            <div><br/></div>
            <input class="btn-submit" type="button" onclick="location.reload();" value="Refresh">
        </div>

    </div>
</div>


<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>

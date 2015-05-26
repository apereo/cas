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

<div class="row">
    <div class="col-sm-12">
        <table class="table table-striped table-hover services-table">
            <thead>
                <tr>
                <th class="col-sm-4"><spring:message code="management.services.manage.label.name" /></th>
                <th class="col-sm-4"><spring:message code="management.services.manage.label.serviceUrl" /></th>
                <th class="col-sm-2"><spring:message code="management.services.manage.label.evaluationOrder" /></th>
                <th class="col-sm-1"></th>
                <th class="col-sm-1"></th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${services}" var="service" varStatus="status">
                <tr id="row${status.index}"${param.id eq service.id ? ' class="added"' : ''}>
                    <td id="${service.id}">${service.name}</td>
                    <td>${fn:length(service.serviceId) < 100 ? service.serviceId : fn:substring(service.serviceId, 0, 100)}</td>
                    <td>${service.evaluationOrder}</td>
                    <td id="edit${status.index}">
                        <button class="btn btn-success" ng-click="action.selectAction('add')">
                            <i class="fa fa-lg fa-pencil"></i> <spring:message code="management.services.manage.action.edit" />
                        </button>
                    </td>
                    <td id="delete${status.index}">
                        <button class="btn btn-danger" onclick="javascript:;">
                            <i class="fa fa-lg fa-trash"></i> <spring:message code="management.services.manage.action.delete" />
                        </button>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
        </table>  <!-- end .services-table table -->
    </div> <!-- end .col-sm-12 div -->
</div> <!-- end .row div -->
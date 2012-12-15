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
<%@include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp"%>

<h2>Runtime Statistics</h2>
<table width="800">
    <thead>
        <tr>
            <th>Property</th>
            <th>Value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Server</td>
            <td>${serverIpAddress} (${serverHostName})</td>
        </tr>
        <tr>
            <td>CAS Ticket Suffix</td>
            <td>${casTicketSuffix}</td>
        </tr>
        <tr>
            <td>Server Start Time</td>
            <td>${startTime}</td>
        </tr>
        <tr>
            <td>Uptime</td>
            <td>${upTime}</td>
        </tr>
        <tr>
            <td>Memory</td>
            <td> ${freeMemory} MB free <img src="images/green.gif" width="${freeMemory / (totalMemory) * 500}" height="30" /><img src="images/red.gif" width="${(totalMemory - freeMemory) / totalMemory * 500}" height="30" /> ${totalMemory} MB total </td>
        </tr>
        <tr>
            <td>Maximum Memory</td>
            <td>${maxMemory} MB</td>
        </tr>
        <tr>
            <td>Available Processors</td>
            <td>${availableProcessors}</td>
        </tr>
    </tbody>
</table>

<br /><br />

<h2>Ticket Registry Statistics</h2>
<table width="800">
    <thead>
        <tr>
            <th>Property</th>
            <th>Value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Unexpired TGTs</td>
            <td>${unexpiredTgts}</td>
        </tr>
        <tr>
            <td>Unexpired STs</td>
            <td>${unexpiredSts}</td>
        </tr>
        <tr>
            <td>Expired TGTs</td>
            <td>${expiredTgts}</td>
        </tr>
        <tr>
            <td>Expired STs</td>
            <td>${expiredSts}</td>
        </tr>
    </tbody>
</table>

<h2>Performance Statistics</h2>

<c:forEach items="${graphingStatisticAppenders}" var="appender">
<h3>${appender.name}</h3>

<img src="${appender.chartGenerator.chartUrl}" alt="${appender.name}" />

</c:forEach>

<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>
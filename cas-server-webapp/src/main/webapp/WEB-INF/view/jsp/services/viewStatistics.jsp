<%@include file="includes/top.jsp"%>
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
            <td>Server Start Time</td>
            <td>${startTime}</td>
        </tr>
        <tr>
            <td>Uptime</td>
            <td>${upTime}</td>
        </tr>
        <tr>
            <td>Memory</td>
            <td> ${freeMemory} MB free <img src="../images/green.gif" width="${freeMemory / (totalMemory) * 500}" height="30" /><img src="../images/red.gif" width="${(totalMemory - freeMemory) / totalMemory * 500}" height="30" /> ${totalMemory} MB total </td>
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

<%@include file="includes/bottom.jsp" %>
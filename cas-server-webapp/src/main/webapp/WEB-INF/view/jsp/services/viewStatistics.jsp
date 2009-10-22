<%@include file="includes/top.jsp"%>
<h2>Runtime Statistics</h2>
<table>
    <thead>
        <tr>
            <th>Property</th>
            <th>Value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Total Memory</td>
            <td>${totalMemory} MB</td>
        </tr>
        <tr>
            <td>Maximum Memory</td>
            <td>${maxMemory} MB</td>
        </tr>
        <tr>
            <td>Free Memory</td>
            <td>${freeMemory} MB</td>
        </tr>
        <tr>
            <td>Available Processors</td>
            <td>${availableProcessors}</td>
        </tr>
    </tbody>
</table>
<br /><br />

<h2>Ticket Registry Statistics</h2>
<table>
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
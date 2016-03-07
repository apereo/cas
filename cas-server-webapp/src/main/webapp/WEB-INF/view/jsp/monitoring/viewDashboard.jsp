<%@include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp"%>

<div class="viewDashboard">

    <h1>CAS Dashboard</h1>
    
    <ul>
        <li><a href="/cas/status">Status</a></li>
        <li><a href="/cas/status/autoconfig">Auto Configuration</a></li>
        <li><a href="/cas/status/beans">Beans</a></li>
        <li><a href="/cas/status/mappings">Endpoint Mappings</a></li>
        <li><a href="/cas/status/configprops">Configuration Properties</a></li>
        <li><a href="/cas/status/dump">Thread Dump</a></li>
        <li><a href="/cas/status/env">Environment</a></li>
        <li><a href="/cas/status/health">Health</a></li>
        <li><a href="/cas/status/info">Info</a></li>
        <li><a href="/cas/status/metrics">Metrics</a></li>
        <li><a href="/cas/status/stats">Statistics Panel</a></li>
        <li><a href="/cas/status/config">Configuration Panel</a></li>
    </ul>
    
    <hr>

    <form method="post" action="/cas/status/restart">
        <button type="submit">Restart</button>
    </form>

    <form method="post" action="/cas/status/refresh">
        <button type="submit">Refresh</button>
    </form>

    <form method="post" action="/cas/status/shutdown">
        <button type="submit">Shutdown</button>
    </form>
    
    <hr>

    <ul>
        <li><a href="/cas/settings/cas/default">Configuration Server: Default Profile</a></li>
        <li><a href="/cas/settings/cas/native">Configuration Server: Native Profile</a></li>
    </ul>
    
</div>

<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>

<%@include file="includes/top.jsp"%>
<h2>By Minute</h2>
<table>
	<thead>
		<tr>
			<th>Time Period</th>
			<th>Week Day</th>
			<th>Hour</th>
			<th>Minute</th>
			<th>Minimum</th>
			<th>Maximum</th>
			<th>Average</th>
			<th>Event Type</th>
			<th>Current Amount</th>
		</tr>
	</thead>
	<tbody>
<c:forEach items="${currentStatistics}" var="statistic">
		<tr>
			<td>${statistic.timePeriod.name}</td>
			<td>${weekdays[statistic.weekDay]}</td>
			<td>${statistic.hour}</td>
			<td>${statistic.minute}</td>
			<td>${statistic.minimum}</td>
			<td>${statistic.maximum}</td>
			<td>${statistic.average}</td>
			<td>${statistic.eventType}</td>
			<td>${statistic.currentValue}</td>
		</tr>
</c:forEach>
	</tbody>
</table>	
<%@include file="includes/bottom.jsp"%>
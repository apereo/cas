<%@include file="includes/top.jsp"%>
<h2>By Day</h2>
<table>
	<thead>
		<tr>
			<td>Time Period</td>
			<td>Week Day</td>
			<td>Minimum</td>
			<td>Maximum</td>
			<td>Average</td>
			<td>Event Type</td>
		</tr>
	</thead>
	<tbody>
<c:forEach items="${statisticsByDay}" var="statistic">
		<tr>
			<td>${statistic.timePeriod.name}</td>
			<td>${weekdays[statistic.weekDay]}</td>
			<td>${statistic.minimum}</td>
			<td>${statistic.maximum}</td>
			<td>${statistic.average}</td>
			<td>${statistic.eventType}</td>
		</tr>
</c:forEach>
	</tbody>
</table>	

<h2>By Hour</h2>
<table>
	<thead>
		<tr>
			<td>Time Period</td>
			<td>Week Day</td>
			<td>Hour</td>
			<td>Minimum</td>
			<td>Maximum</td>
			<td>Average</td>
			<td>Event Type</td>
		</tr>
	</thead>
	<tbody>
<c:forEach items="${statisticsByHour}" var="statistic">
		<tr>
			<td>${statistic.timePeriod.name}</td>
			<td>${weekdays[statistic.weekDay]}</td>
			<td>${statistic.hour}</td>
			<td>${statistic.minimum}</td>
			<td>${statistic.maximum}</td>
			<td>${statistic.average}</td>
			<td>${statistic.eventType}</td>
		</tr>
</c:forEach>
	</tbody>
</table>

<h2>By Minute</h2>
<table>
	<thead>
		<tr>
			<td>Time Period</td>
			<td>Week Day</td>
			<td>Hour</td>
			<td>Minute</td>
			<td>Minimum</td>
			<td>Maximum</td>
			<td>Average</td>
			<td>Event Type</td>
		</tr>
	</thead>
	<tbody>
<c:forEach items="${statisticsByMinute}" var="statistic">
		<tr>
			<td>${statistic.timePeriod.name}</td>
			<td>${weekdays[statistic.weekDay]}</td>
			<td>${statistic.hour}</td>
			<td>${statistic.minute}</td>
			<td>${statistic.minimum}</td>
			<td>${statistic.maximum}</td>
			<td>${statistic.average}</td>
			<td>${statistic.eventType}</td>
		</tr>
</c:forEach>
	</tbody>
</table>	
<%@include file="includes/bottom.jsp"%>
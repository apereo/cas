---
layout: default
title: CAS - Dependency Versions
category: Installation
---

{% include variables.html %}

# Dependency Versions

The following libraries and modules are used by CAS:

<div>

{% assign allVersions = site.data[siteDataVersion]["dependency-versions"] %}

<table class="cas-datatable paginated-table">
	<thead>
		<tr>
			<th>Group</th>
			<th>Name</th>
			<th>Version</th>
		</tr>
	</thead>
	<tbody>
		{% for cfg in allVersions %}
		{% assign configBlock = cfg[1] %}
		{% for cmd in configBlock %}
			<tr>
				<td>
					<code>{{ cmd.group }}</code>
				</td>
				<td>
					<code>{{ cmd.name }}</code>
				</td>
				<td>
					<code>{{ cmd.version }}</code>
				</td>
			</tr>
		{% endfor %}
		{% endfor %}
	</tbody>
</table>
</div>

You may not immediately see all the listed artifacts and libraries in your deployment. Depending on which CAS feature module is included in the final build,
the above libraries and dependencies will be pulled into the final web application artifact. 

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
		{% assign configBlock = cfg[1] | sort: "name" %}
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

You may not immediately see all the listed artifacts and libraries in your
deployment. Depending on which CAS feature module is included in the final build,
the above libraries and dependencies will be pulled into the final web application artifact.

<div class="alert alert-info">:information_source: <strong>Ownership</strong><p>
Please note that the above listed dependencies are generally those that are explicitly requested, owned and controlled
by the CAS software. There are of course many other modules and dependencies that would be <i>transitively</i> pulled in
whose specifics and versions are not directly controlled by CAS. In such scenarios, the specific feature module or library itself is
responsible for pulling its own dependencies correctly into the build. What is listed above should mainly serve as a helpful guide
and not a comprehensive reference.
</p></div>

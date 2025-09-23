---
layout: default
title: CAS - Release Notes
category: Planning
---

{% include variables.html %}

# Release Notes

<table class="cas-datatable">
  <thead>
    <tr><th>Release</th><th>Reference</th></tr>
  </thead>
  <tbody>

    {% for i in (1..4) %}
        <tr>
            <td>RC{{ i }}</td>
            <td><a href="RC{{ i }}.html">See this guide</a>.</td>
        </tr>
    {% endfor %}

  </tbody>
</table>

To understand the release timeline better, please see [CAS releases](https://github.com/apereo/cas/releases).

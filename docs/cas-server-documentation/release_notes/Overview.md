---
layout: default
title: CAS - Release Notes
category: Planning
---

{% include variables.html %}

# Release Notes

<table class="cas-datatable" data-page-length="10">
  <thead>
    <tr>
      <th>Release</th>
      <th>Reference</th>
    </tr>
  </thead>
  <tbody>
    {% for i in (1..6) %}
      {% assign rc_filename = "RC" | append: i | append: ".html" %}
      {% assign rc_url = page.dir | append: rc_filename %}
      {% assign rc_page = site.pages | where: "url", rc_url | first %}

      {% if rc_page %}
        <tr>
          <td>RC{{ i }}</td>
          <td>
            <a href="RC{{ i }}.html">See this guide</a>.
          </td>
        </tr>
      {% endif %}
    {% endfor %}
  </tbody>
</table>

To understand the release timeline better, please see [CAS releases](https://github.com/apereo/cas/releases).

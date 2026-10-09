---
layout: default
title: CAS - Release Notes
description: "Release notes for each release candidate of the current CAS development line, with new features, changes and fixes."
category: Planning
---

{% include variables.html %}

# Release Notes

Each release candidate of the current development line, newest first, and every change across all of them on one page. Every page opens with what changed,
what to review before upgrading and the highlights, followed by every change, which you can filter by area and type.

{% assign releases = site.data.cas_release_notes[page.dir] %}
{% assign combined = site.data.cas_release_aggregate[page.dir] %}
{::nomarkdown}
{%- if releases.size > 0 %}
<ul class="cas-release-index">
    {%- if combined %}
    <li class="cas-release-index-all">
        <h2 id="all-changes"><a href="{{ combined.url }}">All Changes</a></h2>
        <div>
            <p>Every {{ combined.version | escape }} change from {{ combined.releases | join: ', ' }} on one page, each tagged with the release candidate that introduced it.</p>
        </div>
        <p class="cas-release-index-stats">
            <span><b>{{ combined.stats.topics }}</b>topics</span>
            <span><b>{{ combined.stats.upgrade }}</b>to review</span>
            <span><b>{{ combined.stats.others }}</b>other changes</span>
        </p>
    </li>
    {%- endif %}
    {%- for release in releases %}
    <li>
        <h2 id="{{ release.label | downcase }}"><a href="{{ release.url }}">{{ release.version | escape }}</a></h2>
        <div>
            {%- if release.summary %}<p>{{ release.summary | markdownify | remove: '<p>' | remove: '</p>' | strip }}</p>{% endif %}
            {%- if release.highlights.size > 0 %}<p class="cas-release-index-topics">{{ release.highlights | join: ' · ' | escape }}</p>{% endif %}
        </div>
        <p class="cas-release-index-stats">
            <span><b>{{ release.stats.topics }}</b>topics</span>
            <span><b>{{ release.stats.upgrade }}</b>to review</span>
            <span><b>{{ release.stats.others }}</b>other changes</span>
        </p>
    </li>
    {%- endfor %}
</ul>
{%- else %}
<ul>
    {%- for i in (1..9) %}
    {%- assign rc_url = page.dir | append: "RC" | append: i | append: ".html" -%}
    {%- assign rc_page = site.pages | where: "url", rc_url | first -%}
    {%- if rc_page %}<li><a href="RC{{ i }}.html">RC{{ i }}</a></li>{% endif %}
    {%- endfor %}
</ul>
{%- endif %}
{:/nomarkdown}

To understand the release timeline better, please see [CAS releases](https://github.com/apereo/cas/releases).

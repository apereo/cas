---
layout: default
title: CAS - Configuration Properties
category: Configuration
---

{% include variables.html %}

# Configuration Properties

Search the CAS configuration catalog for settings and their descriptions. The catalog covers
CAS settings as well as those offered by third-party libraries and frameworks such as Spring and Spring Boot.
Press <kbd>Shift</kbd> twice on any page to search it from there.

<div id="cas-settings" class="cas-settings">
    <label class="cas-settings-query">
        <i class="fa fa-magnifying-glass" aria-hidden="true"></i>
        <span class="visually-hidden">Search settings</span>
        <input type="search" autocomplete="off" spellcheck="false"
               placeholder="Setting name, environment variable or keyword, e.g. cas.server.name or ticket expiration">
        <span class="cas-settings-shortcut" aria-hidden="true"><kbd>⇧</kbd><kbd>⇧</kbd></span>
    </label>
    <div class="cas-settings-controls">
        <div class="cas-settings-scope" role="group" aria-label="Search in">
            <button type="button" data-scope="name" aria-pressed="false">Names</button>
            <button type="button" data-scope="all" aria-pressed="true">Names &amp; descriptions</button>
        </div>
        <div class="cas-settings-kinds" role="group" aria-label="Show">
            <button type="button" data-kind="" aria-pressed="true">All</button>
            <button type="button" data-kind="cas" aria-pressed="false">CAS</button>
            <button type="button" data-kind="thirdparty" aria-pressed="false">Third party</button>
        </div>
        <label class="cas-settings-toggle"><input type="checkbox" data-option="exact"> Exact name</label>
        <label class="cas-settings-toggle"><input type="checkbox" data-option="deprecated"> Hide deprecated</label>
        <div class="cas-properties-format" role="group" aria-label="Show settings as">
            <button type="button" data-format="properties" aria-pressed="true">.properties</button>
            <button type="button" data-format="yaml" aria-pressed="false">YAML</button>
            <button type="button" data-format="env" aria-pressed="false">Env vars</button>
        </div>
    </div>
    <p class="cas-settings-status" role="status" aria-live="polite">Loading the configuration catalog…</p>
    <div class="cas-properties cas-properties-enhanced cas-settings-results">
        <div class="cas-properties-list"></div>
    </div>
    <button type="button" class="cas-settings-more" hidden>Show more</button>
    <div class="cas-settings-examples">
        <span>Try</span>
        <button type="button" data-example="cas.server.name">cas.server.name</button>
        <button type="button" data-example="CAS_TGC_CRYPTO_ENABLED">CAS_TGC_CRYPTO_ENABLED</button>
        <button type="button" data-example="ticket expiration">ticket expiration</button>
        <button type="button" data-example="cas.authn.ldap[0].ldap-url">cas.authn.ldap[0].ldap-url</button>
    </div>
</div>

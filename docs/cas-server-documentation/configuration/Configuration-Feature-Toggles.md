---
layout: default
title: CAS - Configuration Feature Toggles
category: Configuration
---

{% include variables.html %}

# Configuration Feature Toggles

You can control the list of auto-configuration classes to exclude them in the `cas.properties` file:

```properties
spring.autoconfigure.exclude=org.apereo.cas.custom.config.SomethingConfigurationClass
```

This is a very granular, internal way to control the auto-configuration classes, but it comes with a few important caveats: 

- It requires you to know the exact class name of the auto-configuration class you want to exclude.
- The auto-configuration classes are entirely internal to CAS and they are subject to change or removal without notice. 
- You may need to hunt down all auto-configuration classes applicable to a feature or behavior and list them for exclusion.

In short, while this method is very powerful, it requires that you know the internal workings of CAS 
and its auto-configuration classes, and it may be difficult to maintain across CAS releases.

Alternatively, it may be desirable to entirely disable a feature altogether by excluding 
all applicable auto-configuration classes without having to identify all of them. This can be done using 
feature toggles that may be set to `true` or `false` in the CAS configuration:

<table class="cas-datatable" data-page-length="15">
    <thead>
    <th>Feature</th>
    <th>Property</th>
    </thead>
    <tbody>
        {% for module in site.data[siteDataVersion]["features"] %}
            {% assign moduleEntry = module[1] | sort: "feature" %}
            {% for cfg in moduleEntry %}
                <tr>
                    <td><code data-bs-toggle="tooltip" 
                        data-bs-placment="top" data-bs-html="true" 
                        title="<code>{{ cfg.type }}</code>">{{ cfg.feature }}</code>
                    </td>
                    <td>
                        {% unless cfg.enabledByDefault %}
                        <i class="fa fa-info-circle"  data-bs-toggle="tooltip" 
                            data-bs-placment="top" data-bs-html="true" 
                            title="If selected, this configuration feature toggle must be enabled explicitly and defined in CAS configuration sources.">
                        </i>
                        {% endunless %}
                        <code>{{ cfg.property }}</code>
                    </td>
                </tr>
            {% endfor %}
        {% endfor %}
    </tbody>
</table>

<div class="alert alert-info mt-3">:information_source: <strong>Usage</strong><p>Note that not every single CAS feature 
may be registered in the <i>Feature Catalog</i> and as such regarded as a standalone feature. The catalog continues to grow throughout the 
CAS release lifecycle to recognize more modules as grouped distinct features, allowing for a one-shop store to disable or enable a given CAS feature.</p></div>

Feature toggles may be specified in the CAS configuration source(s), i.e. `cas.properties` or `cas.yml` file
like any other property, or they may be specified as environment variables or system properties. They are 
treated exactly the same as any other property and are processed during application context initialization. 

Note that the above setting enforces conditional access to the auto-configuration class where a whole suite of `@Bean`s would be 
included or excluded in the application context upon initialization and startup. Conditional inclusion or exclusion of beans 
generally has consequences when it comes to `@RefreshScope` and [supporting refreshable beans](Configuration-Management-Reload.html). 
Note that feature modules are *not refreshable* at this point; they are processed on startup and will either be included in the assembled 
application context or skipped entirely, depending on the result of the enforced condition.

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
     
While the above allows control over individual auto-configuration classes, in some cases it may be desirable
to entirely disable a feature altogether by excluding all applicable auto-configuration classes without having to
identify all of them. This can be done using the following feature toggles:

<table class="cas-datatable">
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

<div class="alert alert-info mt-3"><strong>Usage</strong><p>Note that not every single CAS feature may be registered in the <i>Feature Catalog</i> and as such regarded as a standalone feature. The catalog continues to grow throughout the CAS release lifecycle to recognize more modules as grouped distinct features, allowing for a one-shop store to disable or enable a given CAS feature.</p></div>

Note that the above setting enforces conditional access to the auto-configuration class where a whole suite of `@Bean`s would be included or excluded in the application context upon initialization and startup. Conditional inclusion or exclusion of beans generally has consequences when it comes to `@RefreshScope` and [supporting refreshable beans](Configuration-Management-Reload.html). Note that feature modules are *not refreshable* at this point; they are processed on startup and will either be included in the assembled application context or skipped entirely, depending on the result of the enforced condition.

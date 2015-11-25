---
layout: default
title: CAS - JSON Service Registry
---

# JSON Service Registry
This DAO reads services definitions from JSON configuration files at the application context initialization time.
JSON files are
expected to be found inside a configured directory location and this DAO will recursively look through
the directory structure to find relevant JSON files.

{% highlight xml %}
<alias name="jsonServiceRegistryDao" alias="serviceRegistryDao" />
{% endhighlight %}

Path to the JSON service definitions directory is controlled via:

{% highlight properties %}
service.registry.config.location=classpath:services
{% endhighlight %}

A sample JSON file follows:

{% highlight json %}
{
  "@class" : "org.jasig.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "evaluationOrder" : 0
}
{% endhighlight %}

<div class="alert alert-warning"><strong>Clustering Services</strong><p>
You MUST consider that if your CAS server deployment is clustered, each CAS node in the cluster must have
access to the same set of JSON configuration files as the other, or you may have to devise a strategy to keep
changes synchronized from one node to the next.
</p></div>

The JSON service registry is also able to auto detect changes to the specified directory. It will monitor changes to recognize
file additions, removals and updates and will auto-refresh CAS so changes do happen instantly.

<div class="alert alert-info"><strong>Escaping Characters</strong><p>
Please make sure all field values in the JSON blob are correctly escaped, specially for the service id. If the service is defined as a regular expression, certain regex constructs such as "." and "\d" need to be doubly escaped.
</p></div>


The naming convention for new JSON files is recommended to be the following:

{% highlight bash %}

JSON fileName = serviceName + "-" + serviceNumericId + ".json"

{% endhighlight %}


Based on the above formula, for example the above JSON snippet shall be named: `testJsonFile-103935657744185.json`

<div class="alert alert-warning"><strong>Duplicate Services</strong><p>
As you add more files to the directory, you need to be absolutely sure that no two service definitions
will have the same id. If this happens, loading one definition will stop loading the other. While service ids
can be chosen arbitrarily, make sure all service numeric identifiers are unique. CAS will also output warnings
if duplicate data is found.
</p></div>

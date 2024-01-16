---
layout: default
title: CAS - Webflow Decorations
category: Webflow Management
---

{% include variables.html %}

# Webflow Decorations

There are times where you may need to modify the CAS webflow to include 
additional data, typically fetched from outside resources and endpoints that may 
also be considered sensitive and may require credentials for access. Examples 
include displaying announcements on the CAS login screen or other types of 
dynamic data. You can certainly [extend the webflow](Webflow-Customization-Extensions.html) 
to include additional states and actions into the login flow to call upon endpoints 
and data sources to fetch data. An easier option would be to let CAS decorate the 
webflow automatically by reaching out to your REST endpoints, etc, taking 
care of the internal webflow configuration. Such decorators specifically get 
called upon as CAS begins to render the login view while reserving the right to
decorate additional parts of the webflow in the future.

Note that decorators only inject data into the webflow context where that data 
later on becomes available to the CAS view, and more. Once the data is 
available, you still have the responsibility of using that data to properly 
display it in the appropriate view and style it correctly.

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
Remember that objects and data put into the webflow context must always be <code>Serializable</code>. Complex
objects and data structures that do not pass the serialization requirement will most likely break the flow.</p></div>

## Decorators

The following decorators are available:

| Option | Reference                                                       |
|--------|-----------------------------------------------------------------|
| Groovy | [See this guide](Webflow-Customization-Decorators-Groovy.html). |
| REST   | [See this guide](Webflow-Customization-Decorators-REST.html).   |
       
Note that decorators are typically executed prior to the webflow runtime executing a particular action, and they are
almost always executed for all webflow actions *regardless of the owning flow*.
This gives you the option to decorate all CAS webflows as well as subflows with additional data, if necessary
and you are not limited to decorating just the login flow or the login page.

## Custom

It is possible to design and inject your webflow decorator into CAS using 
the following `@Bean` that would be registered in a `@AutoConfiguration` class:

```java
@Bean
public WebflowDecorator myWebflowDecorator() {
    return new MyWebflowDecorator();
}
```

Your configuration class needs to be registered
with CAS. [See this guide](../configuration/Configuration-Management-Extensions.html) for better details.

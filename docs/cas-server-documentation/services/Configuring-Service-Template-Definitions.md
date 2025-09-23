---
layout: default
title: CAS - Service Template Definitions
category: Services
---

{% include variables.html %}

# Service Template Definitions

A registered service template definition is the foundation and initial building block to construct a service definition.
Acting as a blueprint, a template definition will specify a framework for what a given registered service definition might look like.
For example, a service template definition might want to specify a collection common settings and application policies for a given
service type, such as attribute release and consent policies, to remove the need for those policies to be specified yet again in future service definitions 
that might stem from that blueprint. Concrete service definitions will automatically *inherit* from future improvements/changes to the template.

A few important considerations:

- Concrete service definitions always have the ability to override the template definition and enforce their own policy and settings to allow for exceptions.
  Their version of the configuration and policy will always override the base template. The merging process is not exactly fine-tuned to pick out individual 
  differences in configuration blocks.
- Concrete service definitions may link up with a template definition using their **template name** and **type**. This design choice allows the CAS deployer to 
  define multiple service definition blueprints and templates for the same type of CAS applications with different names.
- Service definitions are not required to build and spin off of a blueprint and can remain and function in a standalone manner.
- The relationship and inheritance hierarchy between a template definition and concrete definitions is fixed at one level or degree and is not recursive. 
  However, composition is favored over inheritance and you may assign multiple template definition names to a concrete service in a comma-separated fashion.

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
Template service definitions work for and apply to all types of registered service definitions known to CAS and
are not restricted to a specific type or protocol. The resulting service definition after the merge operation is always 
internal to CAS, and is not something you can manage and/or maintain separately.</p></div>

The directory location of template service definitions needs to be taught to CAS via settings. This directory is
expected to hold `.json` service definition files that structurally are no different than any given registered service in CAS.
The directory is searched for template definitions recursively, and you may come up with your own directory structure to group
definitions by type, application, etc.

Remember that a service template definition filename **MUST** match the template name itself. The formula for naming template definition files
should be: 

```bash
templateFileName = templateName + ".json"
```

## Configuration

{% include_cached casproperties.html properties="cas.service-registry.templates" %}
    
Please note that processing template service definitions requires scripting. To prepare CAS to support and 
integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

## Examples

Consider the following base template service definition, stored in a `AllLibraryApplications.json` file, 
for a yet-to-be-registered CAS application:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "templateName": "AllLibraryApplications",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "email", "username" ] ],
    "consentPolicy": {
      "@class": "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
      "includeOnlyAttributes": ["java.util.LinkedHashSet", ["email", "username"]],
      "status": "TRUE"
    }    
  },
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "email",
    "canonicalizationMode" : "LOWER"
  },
  "properties" : {
      "@class" : "java.util.HashMap",
      "prop1": {
        "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
        "values" : [ "java.util.HashSet", [ "false" ] ]
      },
      "prop2" : {
        "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
        "values" : [ "java.util.HashSet", [ "hello-world" ] ]
      }
    }
  }
```
  
The following variations are possible: 

{% tabs svctmpls %}

{% tab svctmpls With Overrides %}

A concrete service definition may link up with a template:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://library.org/app/.+",
  "name": "Library",
  "templateName": "AllLibraryApplications",
  "id": 1,
  "description": "My application",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "givenName",
  }
}
```

The final result, when processed and loaded internally by CAS would be the following definition:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "name": "Library",
  "templateName": "AllLibraryApplications",
  "serviceId": "^https://library.org/app/.+",
  "id": 1,
  "description": "My application",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "email", "username" ] ],
    "consentPolicy": {
      "@class": "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
      "includeOnlyAttributes": ["java.util.LinkedHashSet", ["email", "username"]],
      "status": "TRUE"
    }    
  },
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "givenName",
  },
  "properties" : {
      "@class" : "java.util.HashMap",
      "prop1": {
        "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
        "values" : [ "java.util.HashSet", [ "false" ] ]
      },
      "prop2" : {
        "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
        "values" : [ "java.util.HashSet", [ "hello-world" ] ]
      }
    }
  }
```

{% endtab %}

{% tab svctmpls Without Overrides %}

A concrete service definition may link up with a template:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://library.org/app/.+",
  "name": "Library",
  "templateName": "AllLibraryApplications",
  "id": 1,
  "description": "My application"
}
```

The final result, when processed and loaded internally by CAS would be the following definition:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "name": "Library",
  "templateName": "AllLibraryApplications",
  "serviceId": "^https://library.org/app/.+",
  "id": 1,
  "description": "My application",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "email", "username" ] ],
    "consentPolicy": {
      "@class": "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
      "includeOnlyAttributes": ["java.util.LinkedHashSet", ["email", "username"]],
      "status": "TRUE"
    }    
  },
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "email",
    "canonicalizationMode" : "LOWER"
  },
  "properties" : {
      "@class" : "java.util.HashMap",
      "prop1": {
        "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
        "values" : [ "java.util.HashSet", [ "false" ] ]
      },
      "prop2" : {
        "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
        "values" : [ "java.util.HashSet", [ "hello-world" ] ]
      }
    }
  }
```

{% endtab %}

{% tab svctmpls Multiple Templates %}

A concrete service definition may also specify multiple template names:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://library.org/app/.+",
  "name": "Library",
  "templateName": "AllLibraryApplications,UnknownTemplate,AllGenericApplications",
  "id": 1,
  "description": "My application",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "givenName",
  }
}
```

Template definitions will be applied in the same order as they are defined. Assuming both `AllLibraryApplications` and `AllGenericApplications`
template definition files exist and have been loaded by CAS, the merge process will go through each assigned template sequentially, 
carrying the results of previous merge attempts and will also ignore unknown templates that cannot be found and resolved.

{% endtab %}

{% tab svctmpls <i class="fa fa-file-code px-1"></i>Groovy Templates %}

Service template definitions can be as designed Groovy templates, able to generate text and other constructs dynamically.
The template framework in Groovy uses JSP style `<% %>` script and `<%= %>` expression syntax or 
`GString` style expressions. The variable `out` is bound to the writer that the template is being written to.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

For example consider the following `GroovyTemplate` template definition:

```groovy
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "name": "CAS",
  "description": "${GivenDescription}",
  "templateName": "GroovyTemplate",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "${GivenUsernameAttribute}"
  },
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", ${ AllowedAttributes.collect(it -> "\"$it\"") } ]
  }
}
```

The `description`, `usernameAttribute` and `allowedAttributes` fields will be dynamically constructed 
at the time of building a concrete service definition. The values for each of these fields is expected to be found from
variables `GivenDescription`, `GivenUsernameAttribute`, and `AllowedAttributes` that are to be supplied by the concrete service definition in the 
form of [service properties](Configuring-Service-Custom-Properties.html):

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://library.org/app/.+",
  "name": "Library",
  "templateName": "GroovyTemplate",
  "id": 1000,
  "properties": {
    "@class": "java.util.HashMap",
    "GivenDescription": {
      "@class": "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values": [  "java.util.LinkedHashSet", [ "This is my description"  ] ]
    },
    "AllowedAttributes": {
      "@class": "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values": [ "java.util.LinkedHashSet",  [ "email", "username" ] ]
    },
    "GivenUsernameAttribute": {
      "@class": "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values": [ "java.util.LinkedHashSet",  [ "email" ] ]
    }
  }
}
```
       
After the merge process is completed, the final result would be similar to the following definition:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "name": "CAS",
  "templateName": "GroovyTemplate",
  "id": 1000,
  "description": "This is my description",
  "usernameAttributeProvider": {
    "@class": "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute": "email"
  },
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes": [ "java.util.ArrayList", [ "email",  "username" ] ]
  }
}
```
{% endtab %}

{% endtabs %}

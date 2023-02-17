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
that might stem from that blueprint.

A few important considerations:

- Concrete service definitions always have the ability to override the template definition and enforce their own policy and settings to allow for exceptions.
  Their version of the configuration and policy will always override the base template. The merging process is not exactly fine-tuned to pick out individual 
  differences in configuration blocks.
- Concrete service definitions may link up with a template definition using their **template name** and **type**. This design choice allows the CAS deployer to 
  define multiple service definition blueprints and templates for the same type of CAS applications with different names.
- Service definitions are not required to build and spin off of a blueprint and can remain and function in a standalone manner.

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
Template service definitions work for and apply to all types of registered service definitions known to CAS and
are not restricted to a specific type or protocol.</p></div>

The directory location of template service definitions needs to be taught to CAS via settings. This directory is
expected to hold `.json` service definition files that structurally are no different than any given registered service in CAS.
The directory is searched for template definitions recursively, and you may come up with your own directory structure to group
definitions by type, application, etc. There is no hard requirement for naming template definition files.

## Configuration

{% include_cached casproperties.html properties="cas.service-registry.templates" %}
 
## Example

Consider the following base template service definition for a yet-to-be-registered CAS application:

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
   
A concrete service definition may link up with a template by using the same `templateName` and type:

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

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
The resulting service definition after the merge operation is always internal to CAS, and is not something you can 
manage and/or maintain separately.</p></div>

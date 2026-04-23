---
layout: default
title: CAS - Account Registration
category: Registration
---
                  
{% include variables.html %}

# Account (Self-Service) Registration - Requests

The account registration request expects a number of user inputs initially to kick off the registration process.
For starters, a default set of fields and inputs are expected by CAS out of the box, and as always, such details 
and fields can be described in *metadata* form using a JSON document that matches the following map:

```json
{
  "@class" : "java.util.HashMap",
  "field-name" : {
    "@class" : "org.apereo.cas.acct.AccountRegistrationProperty",
    "name" : "field-name",
    "required" : true,
    "label" : "cas.screen.acct.label.field",
    "title" : "cas.screen.acct.title.field",
    "pattern": ".+",
    "type": "email",
    "values" : [ "java.util.ArrayList", [ "sample@gmail.com", "sample2@hotmail.com" ] ],
    "order": 0
  }
}
```
    
The following fields are supported:

| Field      | Description                                                                                  |
|------------|----------------------------------------------------------------------------------------------|
| `name`     | The name of the input field to display on the registration screen.                           |
| `required` | Whether or not this input is required. Defaults to `false`.                                  |
| `label`    | Key to a message key in the CAS language bundles to describe the label text for this input.  |
| `title`    | Key to a message key in the CAS language bundles to describe the title text for this input.  |
| `pattern`  | Regular expression pattern to force and validate the acceptable pattern for the input value. |
| `type`     | The type of this input field (i.e. `select`, `email`, `phone`, `text`, etc.).                |
| `order`    | The display order of this input on the screen.                                               |
| `values`   | List of values to display in order, when type is set to `select`.                            |

<div class="alert alert-info">:information_source: <strong>Is it possible to...?</strong><p>You must be wondering 
by now whether it's possible to customize the screen and include other types of fields, forms and values. 
In general, you should be able to use JSON metadata to describe additional fields so long as the input field's
type is simple enough and supported. If you have a type that isn't supported by the existing 
metadata, you will need to build the input field and workflows and rules linked to it yourself as custom code.</p></div>

The loading and processing of the user registration metadata and fields can be customized using the following component:

```java
@Bean
public AccountRegistrationPropertyLoader accountMgmtRegistrationPropertyLoader() {
    return new MyAccountRegistrationPropertyLoader(resource);
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.


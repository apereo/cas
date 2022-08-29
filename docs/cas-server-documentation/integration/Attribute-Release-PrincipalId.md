---
layout: default
title: CAS - Releasing Principal Id
category: Attributes
---

{% include variables.html %}

# Principal-Id Attribute

Registered CAS applications are given the ability to allow for configuration of a username attribute provider, which controls what should be the designated 
user identifier that is returned to the application. The user identifier by default is the authenticated CAS principal id, yet it optionally may be based 
off of an existing attribute that is available and resolved for the principal already. 

More practically, username attribute provider is translated and applied in the context of the authentication protocol that is used. For example, this 
component determines what should be placed inside the `<cas:user>` tag in the final CAS validation payload that is returned to the 
application when the authentication flow is in the context of the CAS protocol. Each authentication protocol supported by CAS might have an equivalent
concept that is then mapped and translated by the username attribute provider.

<div class="alert alert-warning"><strong>Principal Id As Attribute</strong><p>You may also return the authenticated principal 
id as an extra attribute in the final CAS validation payload, typically when using the CAS protocol. See <a href="Attribute-Release-Policies.html">this 
guide</a> to learn more.</p></div>

A number of providers are able to perform canonicalization on the final user id returned to transform it
into uppercase/lowercase. This is noted by the `canonicalizationMode` whose allowed values are `UPPER`, `LOWER` or `NONE`.
          
## Providers 

The following providers are available to produce usernames.

| Provider       | Description                                                         |
|----------------|---------------------------------------------------------------------|
| Default        | [See this guide](Attribute-Release-PrincipalId-Default.html).       |
| Attribute      | [See this guide](Attribute-Release-PrincipalId-Attribute.html).     |
| Groovy         | [See this guide](Attribute-Release-PrincipalId-Groovy.html).        |
| Anonymous      | [See this guide](Attribute-Release-PrincipalId-Anonymous.html).     |
| Encrypted      | [See this guide](Attribute-Release-PrincipalId-Encrypted.html).     |
| Script Engines | [See this guide](Attribute-Release-PrincipalId-ScriptEngines.html). |

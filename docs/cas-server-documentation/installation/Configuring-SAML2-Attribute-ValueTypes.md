---
layout: default
title: CAS - SAML2 Attribute Value Types
category: Attributes
---
{% include variables.html %}


# SAML2 Attribute Value Types

By default, attribute value blocks that are created in the final SAML2 
response do not carry any type information in the encoded XML.
You can, if necessary, enforce a particular type for an attribute value per the requirements of the SAML2 service provider, if any.
An example of an attribute that is encoded with specific type information would be:

```xml
<saml2:Attribute FriendlyName="givenName" Name="givenName" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri">
    <saml2:AttributeValue xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xsd:string">HelloWorld</saml2:AttributeValue>
</saml2:Attribute>
```

The following attribute value types are supported:

| Type                                                   | Description                                                                                          |
|--------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| `XSString`                                             | Mark the attribute value type as `string`.                                                           |
| `XSURI`                                                | Mark the attribute value type as `uri`.                                                              |
| `XSBoolean`                                            | Mark the attribute value type as `boolean`.                                                          |
| `XSInteger`                                            | Mark the attribute value type as `integer`.                                                          |
| `XSDateTime`                                           | Mark the attribute value type as `datetime` .                                                        |
| `XSBase64Binary`                                       | Mark the attribute value type as `base64Binary`.                                                     |
| `XSObject`                                             | Skip the attribute value type and serialize the value as a complex XML object/POJO.                  |
| `XSObject`                                             | Skip the attribute value type and serialize the value as a complex XML object/POJO.                  |
| `NameIDType`                                           | Transform the attribute to contain an inline NameID element that matches the `Subject`'s NameID.     |
| `urn:oasis:names:tc:SAML:2.0:nameid-format:persistent` | Transform the attribute to contain an inline *persistent* NameID regardless of the `Subject` NameID. |

...where the types for each attribute would be defined as such:
 
```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name": "SAML Service",
  "metadataLocation" : "../../sp-metadata.xml",
  "id": 1,
  "attributeValueTypes": {
    "@class": "java.util.HashMap",
    "<attribute-name>": "<attribute-value-type>"
  }
}
```
     
## Examples
   
The following examples are available.

### Inline NameID - Linked

The following *partial* configuration will encode the attribute value as a NameID similar to that of the `Subject`'s:

```json
...
"attributeValueTypes": {
    "@class": "java.util.HashMap",
    "urn:oid:1.3.6.1.4.1.5923.1.1.1.10": "NameIDType"
}
...
```

The construction of NameID here is identical to the Subject's `NameID` element and [is described here](Configuring-SAML2-NameID.html).

```xml
<saml2:Subject>
    <saml2:NameID Format="urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"
                  NameQualifier="https://idp-test.example.org/cas/idp"
                  SPNameQualifier="https://testsp3.example.org/shibboleth">lkXqG+QpbLU47hvjVvfiADxEQs0=</saml2:NameID>
</saml2:Subject>

...

<saml2:Attribute FriendlyName="eduPersonTargetedID" Name="urn:oid:1.3.6.1.4.1.5923.1.1.1.10" 
    NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri">
    <saml2:NameID Format="urn:oasis:names:tc:SAML:2.0:nameid-format:persistent" 
                  NameQualifier="https://idp-test.example.org/cas/idp" 
                  SPNameQualifier="https://testsp3.example.org/shibboleth">lkXqG+QpbLU47hvjVvfiADxEQs0=</saml2:NameID>
</saml2:Attribute>
...
```

### Inline NameID - Detached

The following *partial* configuration will encode the attribute value separate and detached 
from the the NameID produced for the `Subject`'s:

```json
...
"attributeValueTypes": {
    "@class": "java.util.HashMap",
    "urn:oid:1.3.6.1.4.1.5923.1.1.1.10": "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"
}
...
```

...which would produce the following response: 

```xml
<saml2:Subject>
    <saml2:NameID
            Format="urn:oasis:names:tc:SAML:2.0:nameid-format:transient"
            NameQualifier="https://idp-test.example.org/cas/idp"
            SPNameQualifier="https://testsp3.example.org/shibboleth">AAdzZWNyZXQx6VkzIjk/ckEDc</saml2:NameID>
</saml2:Subject>

...

<saml2:Attribute FriendlyName="eduPersonTargetedID" Name="urn:oid:1.3.6.1.4.1.5923.1.1.1.10" 
    NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri">
    <saml2:NameID Format="urn:oasis:names:tc:SAML:2.0:nameid-format:persistent" 
                  NameQualifier="https://idp-test.example.org/cas/idp" 
                  SPNameQualifier="https://testsp3.example.org/shibboleth">lkXqG+QpbLU47hvjVvfiADxEQs0=</saml2:NameID>
</saml2:Attribute>
...
```

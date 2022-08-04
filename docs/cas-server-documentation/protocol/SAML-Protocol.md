---
layout: default
title: CAS - SAML v1.1 Protocol
category: Protocols
---

{% include variables.html %}

# SAML v1.1 Protocol

CAS supports the [standardized SAML 1.1 protocol](http://en.wikipedia.org/wiki/SAML_1.1) primarily to:

- Support a method of [attribute release](../integration/Attribute-Release.html)
- [Single Logout](../installation/Logout-Single-Signout.html)

A SAML 1.1 ticket validation response is obtained by validating a ticket via POST at the `/samlValidate` URI.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-saml" %}
 
## Applications

Registering SAML v1.1 applications with CAS is similar to any other CAS applications:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://app.example.org.+",
  "name" : "App",
  "id" : 1,
  "supportedProtocols": [ "java.util.HashSet", [ "SAML1" ] ]
}
```

## Actuator Endpoints
           
The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="samlValidate" %}

## Sample Request

```xml
POST /cas/samlValidate?ticket=
Host: cas.example.com
Content-Length: 491
Content-Type: text/xml

<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Header/>
  <SOAP-ENV:Body>
    <samlp:Request xmlns:samlp="urn:oasis:names:tc:SAML:1.0:protocol" MajorVersion="1"
      MinorVersion="1" RequestID="_192.168.16.51.1024506224022"
      IssueInstant="2002-06-19T17:03:44.022Z">
      <samlp:AssertionArtifact>
        ST-1-u4hrm3td92cLxpCvrjylcas.example.com
      </samlp:AssertionArtifact>
    </samlp:Request>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

## Sample Response

```xml
<?xml version="1.0" encoding="UTF-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Body>
    <saml1p:Response xmlns:saml1p="urn:oasis:names:tc:SAML:1.0:protocol" InResponseTo="...." IssueInstant="2017-08-15T06:30:04.622Z" MajorVersion="1" MinorVersion="1" ResponseID="_bf6957bad275fc74a1c079a445581441">
      <saml1p:Status>
        <saml1p:StatusCode Value="saml1p:Success" />
      </saml1p:Status>
      <saml1:Assertion xmlns:saml1="urn:oasis:names:tc:SAML:1.0:assertion" AssertionID="_d9673d8af414cc9612929480b58cb2a1" IssueInstant="2017-08-15T06:30:04.622Z" Issuer="testIssuer" MajorVersion="1" MinorVersion="1">
        <saml1:Conditions NotBefore="2017-08-15T06:30:04.622Z" NotOnOrAfter="2017-08-15T06:30:05.622Z">
          <saml1:AudienceRestrictionCondition>
            <saml1:Audience>https://google.com</saml1:Audience>
          </saml1:AudienceRestrictionCondition>
        </saml1:Conditions>
        <saml1:AuthenticationStatement AuthenticationInstant="2017-08-15T06:46:43.585Z" AuthenticationMethod="urn:ietf:rfc:2246">
          <saml1:Subject>
            <saml1:NameIdentifier>testPrincipal</saml1:NameIdentifier>
            <saml1:SubjectConfirmation>
              <saml1:ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:artifact</saml1:ConfirmationMethod>
            </saml1:SubjectConfirmation>
          </saml1:Subject>
        </saml1:AuthenticationStatement>
        <saml1:AttributeStatement>
          <saml1:Subject>
            <saml1:NameIdentifier>testPrincipal</saml1:NameIdentifier>
            <saml1:SubjectConfirmation>
              <saml1:ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:artifact</saml1:ConfirmationMethod>
            </saml1:SubjectConfirmation>
          </saml1:Subject>
          <saml1:Attribute AttributeName="testAttribute" AttributeNamespace="whatever">
            <saml1:AttributeValue>testValue</saml1:AttributeValue>
          </saml1:Attribute>
          <saml1:Attribute AttributeName="samlAuthenticationStatementAuthMethod" AttributeNamespace="whatever">
            <saml1:AttributeValue>urn:ietf:rfc:2246</saml1:AttributeValue>
          </saml1:Attribute>
          <saml1:Attribute AttributeName="testSamlAttribute" AttributeNamespace="whatever">
            <saml1:AttributeValue>value</saml1:AttributeValue>
          </saml1:Attribute>
          <saml1:Attribute AttributeName="testAttributeCollection" AttributeNamespace="whatever">
            <saml1:AttributeValue>tac1</saml1:AttributeValue>
            <saml1:AttributeValue>tac2</saml1:AttributeValue>
          </saml1:Attribute>
        </saml1:AttributeStatement>
      </saml1:Assertion>
    </saml1p:Response>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

## Configuration

{% include_cached casproperties.html properties="cas.saml-core" %}

You may also need to declare the following repository in
your CAS Overlay to be able to resolve dependencies:

```groovy
repositories {
    maven { 
        mavenContent { releasesOnly() }
        url "https://build.shibboleth.net/maven/releases/" 
    }
}
```

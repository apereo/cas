---
layout: default
title: CAS - Attribute Release Consent
category: Attributes
---

{% include variables.html %}

# REST - Attribute Consent Storage

Support is enabled by including the following module in the WAR Overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-consent-rest" %}

Endpoints must be designed to accept/process `application/json`.

| Operation                 | Method    | Data                                 | Expected Response
|---------------------------|-----------|--------------------------------------------------------------------------------------
| Locate consent decision   | `GET`     | `service`, `principal` as headers.    | `200`. The consent decision object in the body.
| Locate consent decision for user   | `GET`     | `principal` as header.    | `200`. The consent decisions object in the body.
| Locate all consent decisions  | `GET`     | N/A    | `200`. The consent decisions object in the body.
| Store consent decision    | `POST`    |  Consent decision object in the body. | `200`.
| Delete consent decision   | `DELETE`  | `/<decisionId>` appended to URL. `principal` as header      | `200`.
| Delete consent decisions   | `DELETE`  | `principal` as header.      | `200`.

The consent decision object in transit will and must match the JSON structure above.

## Configuration

{% include casproperties.html properties="cas.consent.rest" %}


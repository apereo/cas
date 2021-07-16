---
layout: default
title: CAS - Configuring Authentication Events
category: Authentication
---
{% include variables.html %}

# Authentication Events

CAS provides a facility for consuming and recording authentication events into 
persistent storage. This functionality is similar to the records
kept by the [Audit log](../audits/Audits.html) except that the functionality and storage 
format is controlled via CAS itself rather than the audit engine.
Additionally, while audit data may be used for reporting and monitoring, events 
stored into storage via this functionality may later be assessed
in a historical fashion to assess authentication requests, evaluate risk 
associated with them and take further action upon them. Events are primarily
designed to be consumed by the developer and subsequent CAS modules, while 
audit data is targeted at deployers for end-user functionality and reporting.

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-core-events" %}

To learn how to configure database drivers, [please see this guide](../installation/JDBC-Drivers.html).

{% include casproperties.html properties="cas.events.core." %}

## Recorded Data

The following metadata is captured and recorded by the event machinery when enabled:

| Field                             | Description
|-----------------------------------|-----------------------------------------------------------------
| `principalId`                              | The principal id of the authenticated subject
| `timestamp`                                | Timestamp of this event
| `creationTime`                             | Timestamp of this authentication event
| `clientIpAddress`                          | Client IP address
| `serverIpAddress`                          | Server IP address
| `agent`                                    | User-Agent of the browser
| `geoLatitude`                              | Geo Latitude of authentication request's origin
| `geoLongitude`                             | Geo Longitude of authentication request's origin
| `geoAccuracy`                              | Accuracy measure of the location
| `geoTimestamp`                             | Timestamp of the geo location request

## GeoLocation

CAS attempts to record the geolocation properties of the authentication requests, by allowing 
the browser to ask for user's consent.  Should consent not be granted or geolocation 
not supported by the browser, CAS will ignore the geolocation data when it attempts to
record the event. To learn more, please [review this guide](GeoTracking-Authentication-Requests.html).

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include actuators.html endpoints="events" %}
  
## Storage

The following options may be used to store events in CAS.

| Storage          | Description                                           
|-------------------------------------------------------------------------
| MongoDb           | [See this guide](Configuring-Authentication-Events-MongoDb.html).   
| DynamoDb          | [See this guide](Configuring-Authentication-Events-DynamoDb.html).   
| Redis             | [See this guide](Configuring-Authentication-Events-Redis.html).   
| CouchDb           | [See this guide](Configuring-Authentication-Events-CouchDb.html).   
| JPA               | [See this guide](Configuring-Authentication-Events-JPA.html).   
| InfluxDb          | [See this guide](Configuring-Authentication-Events-InfluxDb.html).   
| Memory            | [See this guide](Configuring-Authentication-Events-Memory.html).   

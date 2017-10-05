---
layout: default
title: CAS - Configuring Service Expiration Policy
---

# Configure Service Expiration Policy

An application registered with CAS may be assigned an optional expiration policy that controls the lifetime of the registration. Once the service is deemed expired, it will automatically be disabled or removed from the CAS registry and [relevant contacts](Configuring-Service-Contacts.html) defined and assigned to the service will be notified via email or text messages.

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "expirationPolicy": {
    "@class": "org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy",
    "deleteWhenExpired": true,
    "notifyWhenDeleted": false,
    "expirationDate": "2017-10-05"
  }
}
```

The following settings are available by default for expiration policies:

| Field                | Description
|----------------------|-------------------------------------------------------------
| `expirationDate`     | The date on which the registration record is deemed expired.
| `deleteWhenExpired`  | When `true`, removes the application from the CAS service registry if and when expired. Otherwise the application record will be marked as disabled.
| `notifyWhenDeleted`  | Notifies [contacts](Configuring-Service-Contacts.html) of the application via email or text, assuming valid contacts with email addresses or phone numbers are defined and CAS is configured to send [email messages](Sending-Email-Configuration.html) or [SMS notifications](SMS-Messaging-Configuration.html). The notification is only sent if the application is expired and is about to be deleted from the registry.

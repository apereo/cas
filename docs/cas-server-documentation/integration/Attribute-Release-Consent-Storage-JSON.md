---
layout: default
title: CAS - Attribute Release Consent
category: Attributes
---

{% include variables.html %}

# JSON - Attribute Consent Storage

This is the default option, most useful for demo and testing purposes. Consent decisions are all kept 
inside a static JSON resource whose path is taught to CAS via settings.

A sample record follows:

```json
{
   "id": 1000,
   "principal": "casuser",
   "service": "https://google.com",
   "createdDate": [ 2017, 7, 10, 14, 10, 17 ],
   "options": "ATTRIBUTE_NAME",
   "reminder": 14,
   "reminderTimeUnit": "DAYS",
   "attributes": "..."
}
```

The following fields are available:

| Field                     | Description
|---------------------------|-------------------------------------------------------------------------------------------------
| `id`                      | Valid numeric value for existing records.
| `principal`               | The authenticated user id.
| `service`                 | Target application url to which attributes are about to be released.
| `createdDate`             | Date/Time of the decision record.
| `options`                 | Indicates how changes in attributes are determined for this application. (i.e. `ATTRIBUTE_NAME`, `ATTRIBUTE_VALUE`, `ALWAYS`)
| `reminder`                | Indicates the period after which user will be reminded to consent again, in case no changes are found.
| `reminderTimeUnit`        | The reminder time unit (i.e. `MONTHS`, `DAYS`, `HOURS`, etc).
| `attributes`              | Base64 of attribute names for this application, signed and encrypted.

Valid values for `options` include:

| Field                     | Description
|---------------------------|-------------------------------------------------------------------------------------------------
| `ATTRIBUTE_NAME`          | Ask for consent if any of the attribute names change, for instance, in cases where an attribute is added or removed from the release bundle. Consent is ignored if the value of an existing attribute is changed.
| `ATTRIBUTE_VALUE`         | Same as above, except that attributes values are also accounted for and trigger consent, if changed.
| `ALWAYS`                  | Always ask for consent, regardless of change or context.

## Configuration

{% include casproperties.html properties="cas.consent.json" %}

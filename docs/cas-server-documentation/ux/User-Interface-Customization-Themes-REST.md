---
layout: default
title: Themes - User Interface Customization - CAS
category: User Interface
---

{% include variables.html %}

# RESTful Themes - User Interface Customization

Somewhat similar to the above option, you may calculate the final theme name via a REST endpoint of your own design. The 
theme assigned to the service definition needs to point to the location of the REST API. Endpoints must be designed to 
accept/process `application/json` via `GET` requests. A returned status code `200` allows CAS to read the body of the 
response to determine the theme name. Empty response bodies will have CAS switch to the default theme.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://www.example.org",
  "name" : "MyTheme",
  "theme" : "https://themes.example.org",
  "id" : 1000
}
```

The following parameters may be passed to a REST endpoint:

| Parameter | Description                        |
|-----------|------------------------------------|
| `service` | The requesting service identifier. |

---
layout: default
title: CAS - Database Authentication
category: Authentication
---
{% include variables.html %}

# Stored Procedure Database Authentication

Authenticates a user by attempting to invoke a stored procedure, passing it `username` and `password`
fields as input variables. The output is expected to contain a boolean `status` field that indicates the
whether or not the authentication attempt was successful. All other `OUTPUT` parameters are collected by CAS
as principal attributes if the authentication is successful.

{% include_cached casproperties.html properties="cas.authn.jdbc.procedure" %}

An example of a stored procedure for PostgreSQL might look like this:

```sql
CREATE OR REPLACE FUNCTION sp_authenticate(
  username IN VARCHAR,
  password IN VARCHAR,
  OUT status BOOLEAN
)
AS $$
BEGIN
  IF /* verify the user account here... */ THEN
      status := TRUE;
  ELSE
      status := FALSE;
  END IF;
END;
$$ LANGUAGE plpgsql;
```

## Multitenancy

Configuration settings for database authentication can be specified in a multitenant environment.
Please [review this guide](../multitenancy/Multitenancy-Overview.html) for more information.

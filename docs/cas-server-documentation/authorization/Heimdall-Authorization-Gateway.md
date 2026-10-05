---
layout: default
title: CAS - Heimdall - Gateway Integration
description: "Use nginx as a policy enforcement point that asks Heimdall to authorize each request before passing it upstream."
category: Authorization
---

{% include variables.html %}

# Gateway Integration - Heimdall

Heimdall decides, but something else has to enforce the decision. This example uses an nginx reverse proxy as the policy enforcement point in front of an API.

An nginx reverse proxy can act as the policy enforcement point with its `auth_request` module, sending each request
to `/heimdall/authorize` before passing it upstream:

```nginx
map $request_uri $heimdall_unsafe_uri {
    default     0;
    '~["\\\\]'  1;
}

server {
    location /api {
        if ($heimdall_unsafe_uri) {
            return 400;
        }
        auth_request /authorize;
        proxy_pass https://api.example.org;
    }

    location = /authorize {
        internal;
        proxy_method POST;
        proxy_pass_request_body off;
        proxy_pass https://sso.example.org/cas/heimdall/authorize;
        proxy_set_header Content-Type application/json;
        proxy_set_body '{"namespace": "API_EXAMPLE", "method": "$request_method", "uri": "$request_uri", "context": {"client_ip": "$remote_addr"}}';
    }
}
```

The subrequest carries the client's headers, including `Authorization`, and must use `POST`; see the [warning about `Basic` credentials](Heimdall-Authorization-Principal.html)
behind a gateway. nginx does not escape
variables in the request body, so the `map` rejects URIs that contain quotes or backslashes, which could otherwise
change the namespace or other fields. `auth_request` allows the request on a `2xx` response and refuses it on `401`
or `403`; any other status, such as `404` when no resource matches, becomes a `500`.

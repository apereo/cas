# Service HTTP Security Headers

Source: https://apereo.github.io/cas/development/services/Configuring-Service-Http-Security-Headers.html

CAS has ability to control, on a per-service basis, whether certain security-related HTTP headers should be injected into the response. While headers are typically enabled and defined globally as part of the [CAS Security Filter](https://apereo.github.io/cas/development/planning/Security-Guide.html), the strategy described here allows one to disable/enable the injection of these headers for certain applications and service requests and override the global defaults.

## Global Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.http-web-request.cors.enabled` (Required; type `Boolean`; default `false`): Whether CORS should be enabled for http requests.
- `cas.http-web-request.header.enabled` (Required; type `Boolean`; default `true`): Allow CAS to inject and enforce http security headers via an http filter that are outlined here for caching, HSTS, etc.
- `cas.http-web-request.allow-multi-value-parameters` (Optional; type `Boolean`; default `false`): Whether CAS should accept multi-valued parameters in incoming requests. Example block would to prevent requests where more than one `service` parameter is specified.
- `cas.http-web-request.characters-to-forbid` (Optional; type `String`; default `none`): Characters to block in incoming requests. `none` is a special value. Separate characters by a space.
- `cas.http-web-request.cors.allow-credentials` (Optional; type `Boolean`; default `true`): The Access-Control-Allow-Credentials header Indicates whether or not the response to the request can be exposed when the credentials flag is true. When used as part of a response to a preflight request, this indicates whether or not the actual request can be made using credentials. Note that simple GET requests are not preflighted, and so if a request is made for a resource with credentials, if this header is not returned with the resource, the response is ignored by the browser and not returned to web content.
- `cas.http-web-request.cors.allow-headers` (Optional; type `List<String>`): The Access-Control-Allow-Headers header is used in response to a preflight request to indicate which HTTP headers can be used when making the actual request. Default is everything.
- `cas.http-web-request.cors.allow-methods` (Optional; type `List<String>`): The Access-Control-Allow-Methods header specifies the method or methods allowed when accessing the resource. This is used in response to a pre-flight request. The conditions under which a request is pre-flighted are discussed above. Default is everything.
- `cas.http-web-request.cors.allow-origin-patterns` (Optional; type `List<String>`): Comma-separated list of origin patterns to allow. Unlike allowed origins which only supports `*`, origin patterns are more flexible (for example `https://*.example.com`) and can be used when credentials are allowed. When no allowed origin patterns or allowed origins are set, CORS support is disabled.
- `cas.http-web-request.cors.allow-origins` (Optional; type `List<String>`): The Origin header indicates the origin of the cross-site access request or preflight request. The origin is a URI indicating the server from which the request initiated. When credentials are allowed, '*' cannot be used and origin patterns should be configured instead. It does not include any path information, but only the server name.
- `cas.http-web-request.cors.exposed-headers` (Optional; type `List<String>`): The Access-Control-Expose-Headers header lets a server accept headers that browsers are allowed to access.
- `cas.http-web-request.cors.max-age` (Optional; type `Long`; default `3600`): The Access-Control-Max-Age header indicates how long the results of a preflight request can be cached.
- `cas.http-web-request.custom-headers` (Optional; type `Map<String,String>`): Custom response headers to inject into the response as needed.
- `cas.http-web-request.header.cache` (Optional; type `Boolean`; default `true`): When true, will inject the following headers into the response for non-static resources. ``` Cache-Control: no-cache, no-store, max-age=0, must-revalidate Pragma: no-cache Expires: 0 ```
- `cas.http-web-request.header.cache-control-static-resources` (Optional; type `String`; default `css|js|png|txt|jpg|ico|jpeg|bmp|gif`): Files with these extensions are considered static, so they will be cached by browsers. The value is part of a RegEx.
- `cas.http-web-request.header.content-security-policy` (Optional; type `String`; default `script-src 'self' 'unsafe-inline' 'unsafe-eval' https://www.googletagmanager.com https://www.google.com; object-src 'none'; worker-src 'self' blob: 'unsafe-inline';`): Helps you reduce XSS risks on modern browsers by declaring what dynamic resources are allowed to load via a HTTP Header. Header value is made up of one or more directives. Multiple directives are separated with a semicolon. @nonce@ is a specific value which is replaced by a generated random value saved as the request attribute 'contentSecurityPolicyGeneratedNonce'.
- `cas.http-web-request.header.hsts` (Optional; type `Boolean`; default `true`): When true, will inject the following headers into the response: `Strict-Transport-Security: ...`.
- `cas.http-web-request.header.hsts-options` (Optional; type `String`; default `max-age=15768000 ; includeSubDomains`): Control the value of the `Strict-Transport-Security` header.
- `cas.http-web-request.header.xcontent` (Optional; type `Boolean`; default `true`): When true, will inject the following headers into the response: `X-Content-Type-Options: nosniff`.
- `cas.http-web-request.header.xframe` (Optional; type `Boolean`; default `true`): When true, will inject the following headers into the response: `X-Frame-Options: DENY`.
- `cas.http-web-request.header.xframe-options` (Optional; type `String`; default `DENY`): Will inject values into the `X-Frame-Options` header into the response.
- `cas.http-web-request.header.xss` (Optional; type `Boolean`; default `true`): When true, will inject the following headers into the response: `X-XSS-Protection: 1; mode=block`.
- `cas.http-web-request.header.xss-options` (Optional; type `String`; default `1; mode=block`): Will inject values into the `X-XSS-Protection` header into the response.
- `cas.http-web-request.only-post-params` (Optional; type `String`; default `password`): Parameters that are only allowed and accepted during posts.
- `cas.http-web-request.params-to-check` (Optional; type `String`; default `ticket,service,renew,gateway,warn,method,target,SAMLart,pgtUrl,pgt,pgtId,pgtIou,targetService,entityId,token`): Parameters to sanitize and cross-check in incoming requests. Separate parameter names by a comma. The special value * instructs the Filter to check all parameters.
- `cas.http-web-request.pattern-to-block` (Optional; type `String`): Specify a regular expression that would be checked against the request URL. If a successful match is found, the request would be blocked.
- `cas.http-web-request.web.encoding` (Optional; type `String`): Control and specify the encoding for all http requests.
- `cas.http-web-request.web.force-encoding` (Optional; type `Boolean`; default `true`): Whether specified encoding should be forced for every request. Whether the specified encoding is supposed to override existing request and response encodings

## Service HTTP Headers

The following properties are available and recognized by CAS for various modules and features:

| Name | Default Value | Type | Group |
| --- | --- | --- | --- |
| `httpHeaderEnableCacheControl` | `true` | `BOOLEAN` | `HTTP_HEADERS` |
| `httpHeaderEnableXContentOptions` | `true` | `BOOLEAN` | `HTTP_HEADERS` |
| `httpHeaderEnableStrictTransportSecurity` | `true` | `BOOLEAN` | `HTTP_HEADERS` |
| `httpHeaderStrictTransportSecurity` |  | `STRING` | `HTTP_HEADERS` |
| `httpHeaderEnableXFrameOptions` | `true` | `BOOLEAN` | `HTTP_HEADERS` |
| `httpHeaderXFrameOptions` | `DENY` | `STRING` | `HTTP_HEADERS` |
| `httpHeaderEnableContentSecurityPolicy` | `true` | `STRING` | `HTTP_HEADERS` |
| `httpHeaderEnableXSSProtection` | `true` | `BOOLEAN` | `HTTP_HEADERS` |

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "properties" : {
    "@class" : "java.util.HashMap",
    "httpHeaderEnableXContentOptions" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "true" ] ]
    }
  }
}
```

## CORS Policies

Cross-origin resource sharing (CORS) policies can also be defined per application in form of service properties.

The following properties are available and recognized by CAS for various modules and features:

| Name | Default Value | Type | Group |
| --- | --- | --- | --- |
| `corsAllowCredentials` | `false` | `BOOLEAN` | `CORS` |
| `corsMaxAge` |  | `INTEGER` | `CORS` |
| `corsAllowedOrigins` |  | `STRING` | `CORS` |
| `corsAllowedOriginPatterns` |  | `STRING` | `CORS` |
| `corsAllowedMethods` |  | `STRING` | `CORS` |
| `corsAllowedHeaders` |  | `STRING` | `CORS` |
| `corsExposedHeaders` |  | `STRING` | `CORS` |

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "properties" : {
    "@class" : "java.util.HashMap",
    "corsMaxAge" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "1000" ] ]
    }
  }
}
```

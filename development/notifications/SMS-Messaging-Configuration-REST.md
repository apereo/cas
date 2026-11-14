# REST SMS Messaging

Source: https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-REST.html

Send text messages using a RESTful API. This is typically a `POST` that can be sent to an endpoint via the listed styles below. A status code of `2xx` is expected from the endpoint.

**Query Parameters**

The following request/query parameters are submitted to the endpoint URL:

| Field | Description |
| --- | --- |
| `clientIpAddress` | The client IP address, when available. |
| `serverIpAddress` | The server IP address, when available. |
| `from` | The from address of the text message. |
| `to` | The target recipient of the text message. |

The request body contains the actual message.

**Request Body**

In this option, the following parameters are included in the request body as a JSON document:

| Field | Description |
| --- | --- |
| `clientIpAddress` | The client IP address, when available. |
| `serverIpAddress` | The server IP address, when available. |
| `from` | The from address of the text message. |
| `to` | The target recipient of the text message. |
| `text` | The target recipient of the text message. |

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.sms-provider.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.sms-provider.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.sms-provider.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.sms-provider.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.sms-provider.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.sms-provider.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
- `cas.sms-provider.rest.style` (Optional; type `RestfulSmsProperties.RestfulSmsRequestStyles`; default `QUERY_PARAMETERS`): Indicate the style and formatting of the SMS request parameters and how they should be included and sent via REST. Available values are as follows: - `QUERY_PARAMETERS`: This option will submit an SMS http request where the from, to, etc are passed as request query parameters. Only the message itself is included in the request body. - `REQUEST_BODY`: This option will pass all parameters in the body of the SMS http request as a JSON object.

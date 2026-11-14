# Logout and Single Logout (SLO)

Source: https://apereo.github.io/cas/development/installation/Logout-Single-Signout.html

There are potentially many active application sessions during a CAS single sign-on session, and the distinction between logout and single logout is based on the number of sessions that are ended upon a *logout* operation.

> **Protocol Support**
>
> Note that SLO described here specifically deals with the semantics of the CAS protocol. All other available protocols in CAS may offer and behave differently when it comes to handling, receiving and publishing logout requests whether CAS is acting as an identity provider or service provider. SLO support for each protocol implementation may vary and you should always verify the extent of available functionality for each protocol implementation.

The scope of logout is determined by where the action takes place:

1. Application logout - ends a single application session
2. CAS logout - ends the CAS SSO session

Note that the logout action in each case has no effect on the other in the simple case. Ending an application session does not end the CAS session and ending the CAS session does not affect application sessions. This is a common cause of confusion for new users and deployers of an SSO system.

The single logout support in CAS attempts to reconcile the disparity between CAS logout and application logout. When CAS is configured for SLO, it attempts to send logout messages to every application that requested authentication to CAS during the SSO session. While this is a best-effort process, in many cases it works well and provides a consistent user experience by creating symmetry between login and logout.

## CAS Logout

Per the [CAS Protocol](https://apereo.github.io/cas/development/protocol/CAS-Protocol.html), the `/logout` endpoint is responsible for destroying the current SSO session. Upon logout, it may also be desirable to redirect back to a service. This is controlled via specifying the redirect link via the `service` parameter. The specified `service` must be registered in the service registry of CAS and enabled and CAS must be allowed to follow service redirects.

The following settings and properties are available from the CAS configuration catalog:

- `cas.logout.confirm-logout` (Optional; type `Boolean`; default `false`): Before logout, allow the option to confirm on the web interface.
- `cas.logout.follow-service-redirects` (Optional; type `Boolean`; default `false`): Whether CAS should be allowed to redirect to an alternative location after logout.
- `cas.logout.redirect-parameter` (Optional; type `List<String>`): The target destination to which CAS should redirect after logout is indicated and extracted by a parameter name of your choosing here. If none specified, the default will be used as `service`.
- `cas.logout.redirect-url` (Optional; type `String`): A url to which CAS must immediately redirect after all logout operations have completed. Typically useful in scenarios where CAS is acting as a proxy and needs to redirect to an external identity provider's logout endpoint in order to remove a session, etc.

## Single Logout (SLO)

CAS is designed to support single sign out: it means that it will be able to invalidate client application sessions in addition to its own SSO session.
Whenever a ticket-granting ticket is explicitly expired, the logout protocol will be initiated. Clients that do not support the logout protocol may notice extra requests in their access logs that appear not to do anything.

> **Usage Warning!**
>
> Single Logout is turned on by default.

When a CAS session ends, it notifies each of the services that the SSO session is no longer valid, and that relying parties need to invalidate their own session. Remember that the callback submitted to each CAS-protected application is a notification; nothing more. It is the **responsibility of the application** to intercept that notification and properly destroy the user authentication session, either manually, via a specific endpoint or more commonly via a CAS client library that supports SLO.

Also note that since SLO is a global event, all applications that have an authentication record with CAS will by default be contacted, and this may disrupt user experience negatively if those applications are individually distinct from each other. As an example, if user has logged into a portal application and an email application, logging out of one through SLO will also destroy the user session in the other which could mean data loss if the application is not carefully managing its session and user activity.

The following settings and properties are available from the CAS configuration catalog:

- `cas.slo.asynchronous` (Optional; type `Boolean`; default `true`): Whether SLO callbacks should be done in an asynchronous manner via the HTTP client. When true, CAS will not wait for the operation to fully complete and will resume control to carry on.
- `cas.slo.disabled` (Optional; type `Boolean`; default `false`): Whether SLO should be entirely disabled globally for the CAS deployment.
- `cas.slo.logout-propagation-type` (Optional; type `SingleLogoutProperties.LogoutPropagationTypes`; default `AJAX`): Logout propagation type determines how SLO requests will be sent to applications. This is especially applicable when SLO requests are processed using a front-channel mechanism. Available values are as follows: - `AJAX`: This is the default propagation mechanism where logout requests are sent to applications using a AJAX call via `jsonp`. - `IFRAME`: The propagation mechanism will submit the logout request to the logout URL that is loaded inside an iframe. This is typically useful if the application receiving the logout request needs to generate HTML to process the logout request notification, especially if the application is a SPA.

### Back Channel

CAS sends an HTTP POST message directly to the service. This is the traditional way of performing notification to the service.

A sample back channel SLO message:

```xml
<samlp:LogoutRequest
    xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol"
    xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
    ID="[RANDOM ID]"
    Version="2.0"
    IssueInstant="[CURRENT DATE/TIME]">
    <saml:NameID>[PRINCIPAL IDENTIFIER]</saml:NameID>
    <samlp:SessionIndex>[SESSION IDENTIFIER]</samlp:SessionIndex>
</samlp:LogoutRequest>
```

### Front Channel

CAS issues asynchronous AJAX `GET` logout requests via `JSONP` to authenticated services. The expected behaviour of the CAS client is to invalidate the application web session.

> **Usage Warning**
>
> Front channel logout may not be available for all CAS clients. Ensure your CAS client does support this behavior before trying it out.

A sample front channel SLO request submitted by CAS resembles the following format:

```
curl 'https://service.url?callback=jQuery112409319555380089843_1509437967792&logoutRequest=[BASE64 encoded request]&_=1509437967793'
```

## SLO Requests

The way the notification is done (*back* or *front* channel) is configured at a service level through the `logoutType` property. This value is set to `LogoutType.BACK_CHANNEL` by default. The message is delivered or the redirection is sent to the URL presented in the *service* parameter of the original CAS protocol ticket request.

The session identifier is the CAS service ticket ID that was provided to the service when it originally authenticated to CAS. The session identifier is used to correlate a CAS session with an application session; for example, the SLO session identifier maps to a servlet session that can subsequently be destroyed to terminate the application session.

### Redirecting Logout to Service

Logout requests may be optionally routed to an external URL bypassing the CAS logout screen. To do so, you will need to specify the target destination typically in form of a `service` parameter to the CAS logout endpoint per the [CAS protocol specification](https://apereo.github.io/cas/development/protocol/CAS-Protocol-Specification.html).

### Single Logout Per Service

Registered applications with CAS have the option to control single logout behavior individually via the [Service Management](https://apereo.github.io/cas/development/services/Service-Management.html) component. Each registered service in the service registry will include configuration that describes how the logout request should be submitted. This behavior is controlled via the `logoutType` property which allows one to specify whether the logout request should be submitted via back/front channel or turned off for this application.

Sample configuration follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "logoutType" : "BACK_CHANNEL"
}
```

### Service Endpoint for Logout Requests

By default, logout requests are submitted to the original service id collected at the time of authentication. CAS has the option to submit such requests to a specific service endpoint that is different from the original service id, and of course can be configured on a per-service level. This is useful in cases where the application that is integrated with CAS does not exactly use a CAS client that supports intercepting such requests and instead, exposes a different endpoint for its logout operations.

To configure a service specific endpoint, try the following example:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "logoutType" : "BACK_CHANNEL",
  "logoutUrl" : "https://web.application.net/logout"
}
```

### Asynchronous SLO Messages

By default, backchannel logout messages are sent to endpoint in an asynchronous fashion. This behavior can be modified via CAS settings.

## SSO Session vs. Application Session

In order to better understand the SSO session management of CAS and how it regards application sessions, one important note is to be first and foremost considered:

> **CAS is NOT a session manager!**
>
> Application session is the responsibility of the application.

CAS wants to maintain and control the SSO session in the form of the `TicketGrantingTicket` and a TGT id which is shared between the user-agent and the CAS server in the form of a secure cookie.

CAS is not an application session manager in that it is the responsibility of the applications to maintain and control their own application sessions. Once authentication is completed, CAS is typically out of the picture in terms of the application sessions. Therefore, the expiration policy of the application session itself is entirely independent of CAS and may be loosely coordinated and adjusted depending on the ideal user experience in the event that the application session expires.

In the event that Single Logout is not activated, typically, application may expose a logout endpoint in order to destroy the session and next, redirect the agent to the CAS `logout` endpoint in order to completely destroy the SSO session as well.

The example below shows how different application session settings interact with a CAS single sign-on session that has an 8-hour fixed lifetime:

| Application session | When the application session ends while the CAS session is valid | When the application session ends after the CAS session has expired |
| --- | --- | --- |
| 2-hour fixed lifetime | The next request goes to CAS for a new service ticket, without a login prompt. | The next request goes to CAS and the user logs in again. |
| 30-minute sliding (idle) lifetime | The next request after 30 idle minutes goes to CAS for a new service ticket. | The next request goes to CAS and the user logs in again. |
| Opts out of SSO with `renew=true` | Every request to CAS asks the user to log in, whatever the CAS session state. | Same. |

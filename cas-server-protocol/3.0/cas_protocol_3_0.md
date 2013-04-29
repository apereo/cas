CAS Protocol 3.0 {.c7}
================

[1. Introduction](#h.rm13e2vlos7p)

[1.1. Conventions & Definitions](#h.zg25t81ihfd7)

[2. CAS URIs](#h.aucjhvsm4ycn)

[2.1. /login as credential requestor](#h.bxyegr69yke2)

[2.1.1. parameters](#h.jb7qbuony33o)

[2.1.2. URL examples of /login](#h.qx9kfecqt5qz)

[2.1.3. response for username/password authentication](#h.4ox659uzpzcb)

[2.1.4. response for trust authentication](#h.z133zuhyj6fk)

[2.1.5. response for single sign-on authentication](#h.ulrk08lojjm6)

[2.2. /login as credential acceptor](#h.hki1fu7cy5uc)

[2.2.1. parameters common to all types of
authentication](#h.uhl7kem0jmvz)

[2.2.2. parameters for username/password
authentication](#h.tqufnk1xt2wn)

[2.2.3. parameters for trust authentication](#h.pfejyzfop7lt)

[2.2.4. response](#h.z0v98zeh838z)

[2.3. /logout](#h.wd7jjtjt2yfn)

[2.3.1. parameters](#h.qrr5gaqnt1re)

[2.3.2. response](#h.pnpd6rkvjn9q)

[2.3.3 Single Logout](#h.j47tzxlnze4)

[2.3.3.1 Server behaviour](#h.3mvar4ca398f)

[2.3.3.2 Client behaviour](#h.3d8d81dmvljt)

[2.4. /validate [CAS 1.0]](#h.5ns6qdff4mmv)

[2.4.1. parameters](#h.pljvypx6jbs2)

[2.4.2. response](#h.yexft2vrqz2u)

[2.4.3. URL examples of /validate](#h.ojjz6ozg58wf)

[2.5. /serviceValidate [CAS 2.0]](#h.nuz0n6fcsrmk)

[2.5.1. parameters](#h.s674febnxo6g)

[2.5.2. response](#h.ezfpwht1n3or)

[2.5.3. error codes](#h.2ilhb7s8qnp)

[2.5.4. proxy callback](#h.kgn3dplz3p1u)

[2.5.5 Attributes](#h.51xguoz3z4s2)

[2.5.6. URL examples of /serviceValidate](#h.rbci8wlykg8z)

[2.5.7 Example response with attributes](#h.z0kp8cjq1idi)

[2.6. /proxyValidate [CAS 2.0]](#h.6bfdbrdcop9n)

[2.6.1. parameters](#h.x8w6d7ih1byt)

[2.6.2. response](#h.t30xi1kdcau)

[2.6.3 error codes](#h.nprzaeaavq3x)

[2.6.4 URL examples of /proxyValidate](#h.xfot4r4nz91)

[2.7. /proxy [CAS 2.0]](#h.5j9zpfewyz8c)

[2.7.1. parameters](#h.sz2ysdacfjw5)

[2.7.2. response](#h.5xvgmje745cl)

[2.7.3. error codes](#h.fo0ybwh5ak07)

[2.7.4. URL example of /proxy](#h.k4bh1bjxfdjm)

[3. CAS Entities](#h.4w6zujev9swo)

[3.1. service ticket](#h.2uu6v5wnellk)

[3.1.1. service ticket properties](#h.tiauhvnhuu6o)

[3.2. proxy ticket](#h.jriggza5fsy4)

[3.2.1. proxy ticket properties](#h.4bnric3gzokt)

[3.3. proxy-granting ticket](#h.my9m59hyz2tm)

[3.3.1. proxy-granting ticket properties](#h.e2vscdjxry4i)

[3.4. proxy-granting ticket IOU](#h.r1qlaqx8uorp)

[3.4.1. proxy-granting ticket IOU properties](#h.trxw7oeod99u)

[3.5. login ticket](#h.6l8yd2jsm1in)

[3.5.1. login ticket properties](#h.s4hssahr6nfb)

[3.6. ticket-granting cookie](#h.9pti1ys0ikz2)

[3.6.1. ticket-granting cookie properties](#h.j5nkupex5nus)

[3.7. ticket and ticket-granting cookie character set](#h.tn2u4lmgr6sm)

[4. Optional Features](#h.ve5g8i6cxpls)

[4.1 Long-Term Tickets - Remember-Me [CAS 3.0]](#h.r4owowbmyoyb)

[4.1.1 Enabling Remember-Me (Login Page)](#h.43ffz3gnvgmo)

[4.1.2 Security implications](#h.kvqntr1up4fk)

[4.1.3 CAS Validation Response Attributes](#h.ywcwhtqrzd7b)

[4.1.4 CAS Client requirements](#h.oleuicedw0lo)

[4.2 /samlValidate [CAS 3.0]](#h.43o2c6rhr4tx)

[4.2.1 parameters](#h.yisxu5z5s6pj)

[4.2.2 HTTP Request Method and Body](#h.apmr6ogzeiyx)

[4.2.3 SAML request values](#h.u7difdf7nuou)

[4.2.4 Example of /samlValidate POST request](#h.wedxl4sj1g2e)

[4.2.5 SAML response](#h.liq0dnegms2w)

[4.2.5.1 SAML CAS response attributes](#h.9y9e5r9tr7es)

[Appendix A: CAS response XML schema](#h.ndz7vn7rb35s)

[Appendix B: Safe redirection](#h.6z7khnuwm8lt)

[Appendix C: Logout XML document](#h.uwhuso1ebw6q)

[Appendix D: References](#h.dbuf4dc3554c)

[Appendix E: CAS License](#h.lwcapa29bgpm)

[Appendix F: Changes to this Document](#h.1dcq7o794rkv)

Author: Drew Mazurek

Contributors:

1.  Susan Bramhall
2.  Howard Gilbert
3.  Andy Newman
4.  Andrew Petro
5.  Robert Oschwald [CAS3.0]

Version: 3.0

Release Date: NOT\_RELEASED\_YET

Copyright © 2005, Yale University^[[a]](#cmnt1)^
Copyright © 2013, JA-SIG, Inc.^[[a]](#cmnt1)^

1. Introduction {.c7}
---------------

This is the official specification of the CAS 1.0, 2.0 and 3.0
protocols.

### 1.1. Conventions & Definitions {.c7}

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
"SHOULD", "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this
document are to be interpreted as described in RFC 2119[1].

1.  "Client" refers to the end user and/or the web browser.
2.  “CAS Client” refers to the software component that is integrated
    with a web application and interacts with the CAS server via CAS
    protocol.
3.  "Server" refers to the Central Authentication Service server.
4.  "Service" refers to the application the client is trying to access.
5.  "Back-end service" refers to the application a service is trying to
    access on behalf of a client. This can also be referred to as the
    "target service."
6.  “SSO” refers to Single Sign on.
7.  “SLO” refers to Single Logout.
8.  \<LF\> is a bare line feed (ASCII value 0x0a).

2. CAS URIs {.c7}
-----------

CAS is an HTTP[2,3]-based protocol that requires each of its components
to be accessible through specific URIs. This section will discuss each
of the URIs.

### 2.1. /login as credential requestor {.c7}

The /login URI operates with two behaviors: as a credential requestor,
and as a credential acceptor. It responds to credentials by acting as a
credential acceptor and otherwise acts as a credential requestor.

If the client has already established a single sign-on session with CAS,
the web browser presents to CAS a secure cookie containing a string
identifying a ticket-granting ticket. This cookie is called the
ticket-granting cookie. If the ticket-granting cookie keys to a valid
ticket-granting ticket, CAS MAY issue a service ticket provided all the
other conditions in this specification are met. See Section 3.6 for more
information on ticket-granting cookies.

#### 2.1.1. parameters {.c7}

The following HTTP request parameters may be passed to /login while it
is acting as a credential requestor. They are all case-sensitive, and
they all MUST be handled by /login.

1.  service [OPTIONAL] - the identifier of the application the client is
    trying to access. In almost all cases, this will be the URL of the
    application. As a HTTP request parameter, this URL value MUST be
    URL-encoded as described in Section 2.2 of RFC
    3986[4]^[[b]](#cmnt2)^. If a service is not specified and a single
    sign-on session does not yet exist, CAS SHOULD request credentials
    from the user to initiate a single sign-on session. If a service is
    not specified and a single sign-on session already exists, CAS
    SHOULD display a message notifying the client that it is already
    logged in.
2.  renew [OPTIONAL] - if this parameter is set, single sign-on will be
    bypassed. In this case, CAS will require the client to present
    credentials regardless of the existence of a single sign-on session
    with CAS. This parameter is not compatible with the "gateway"
    parameter. Services redirecting to the /login URI and login form
    views posting to the /login URI SHOULD NOT set both the "renew" and
    "gateway" request parameters. Behavior is undefined if both are set.
    It is RECOMMENDED that CAS implementations ignore the "gateway"
    parameter if "renew" is set. It is RECOMMENDED that when the renew
    parameter is set its value be "true".
3.  gateway [OPTIONAL] - if this parameter is set, CAS will not ask the
    client for credentials. If the client has a pre-existing single
    sign-on session with CAS, or if a single sign-on session can be
    established through non-interactive means (i.e. trust
    authentication), CAS MAY redirect the client to the URL specified by
    the "service" parameter, appending a valid service ticket. (CAS also
    MAY interpose an advisory page informing the client that a CAS
    authentication has taken place.) If the client does not have a
    single sign-on session with CAS, and a non-interactive
    authentication cannot be established, CAS MUST redirect the client
    to the URL specified by the "service" parameter with no "ticket"
    parameter appended to the URL. If the "service" parameter is not
    specified and "gateway" is set, the behavior of CAS is undefined. It
    is RECOMMENDED that in this case, CAS request credentials as if
    neither parameter was specified. This parameter is not compatible
    with the "renew" parameter. Behavior is undefined if both are set.
    It is RECOMMENDED that when the gateway parameter is set its value
    be "true".

[](#)

4.  method [OPTIONAL, CAS 3.0] - The method to be used when sending
    responses. While native HTTP redirects (GET) may be utilized as the
    default method, applications that require a POST response can use
    this parameter to indicate the method type. It is up to the CAS
    server implementation to determine whether or not POST responses are
    supported.

#### 2.1.2. URL examples of /login {.c7}

Simple login example:

https://cas.example.org/cas/login?service=http%3A%2F%2Fwww.example.org%2Fservice\

Don't prompt for username/password:

https://cas.example.org/cas/login?service=http%3A%2F%2Fwww.example.org%2Fservice&gateway=true\

Always prompt for username/password:

https://cas.example.org/cas/login?service=http%3A%2F%2Fwww.example.org%2Fservice&renew=true\
\
Use POST responses instead of redirects:

https://cas.example.org/cas/login?method=POST&service=http%3A%2F%2Fwww.example.org%2Fservice\

#### 2.1.3. response for username/password authentication {.c7}

When /login behaves as a credential requestor, the response will vary
depending on the type of credentials it is requesting. In most cases,
CAS will respond by displaying a login screen requesting a username and
password. This page MUST include a form with the parameters, "username",
"password", and "lt". The form MAY also include the parameter, "warn".
If "service" was specified to /login, "service" MUST also be a parameter
of the form, containing the value originally passed to /login. These
parameters are discussed in detail in Section 2.2.1. The form MUST be
submitted through the HTTP POST method to /login which will then act as
a credential acceptor, discussed in Section 2.2.

#### 2.1.4. response for trust authentication {.c7}

Trust authentication accommodates consideration of arbitrary aspects of
the request as a basis for authentication. The appropriate user
experience for trust authentication will be highly deployer-specific in
consideration of local policy and of the logistics of the particular
authentication mechanism implemented.

When /login behaves as a credential requestor for trust authentication,
its behavior will be determined by the type of credentials it will be
receiving. If the credentials are valid, CAS MAY transparently redirect
the user to the service. Alternately, CAS MAY display a warning that
credentials were presented and allow the client to confirm that it wants
to use those credentials. It is RECOMMENDED that CAS implementations
allow the deployer to choose the preferred behavior. If the credentials
are invalid or non-existent, it is RECOMMENDED that CAS display to the
client the reason authentication failed, and possibly present the user
with alternate means of authentication (e.g. username/password
authentication).

#### 2.1.5. response for single sign-on authentication {.c7}

If the client has already established a single sign-on session with CAS,
the client will have presented its HTTP session cookie to /login and
behavior will be handled as in Section 2.2.4. However, if the "renew"
parameter is set, the behavior will be handled as in Section 2.1.3 or
2.1.4.

### 2.2. /login as credential acceptor {.c7}

When a set of accepted credentials are passed to /login, /login acts as
a credential acceptor and its behavior is defined in this Section.

#### 2.2.1. parameters common to all types of authentication {.c7}

The following HTTP request parameters MAY be passed to /login while it
is acting as a credential acceptor. They are all case-sensitive and they
all MUST be handled by /login.

1.  service [OPTIONAL] - the URL of the application the client is trying
    to access. As a HTTP request parameter, this URL value MUST be
    URL-encoded as described in Section 2.2 of RFC
    1738[4]^[[c]](#cmnt3)^. CAS MUST redirect the client to this URL
    upon successful authentication. This is discussed in detail in
    Section 2.2.4. If the CAS Server operates in non-open mode (Service
    URLs allowed to use the CAS Server are registered within the CAS
    Server), the CAS Server MUST deny operation and print out a
    meaningful message if a non-authorized service URL is presented.
2.  warn [OPTIONAL] - if this parameter is set, single sign-on MUST NOT
    be transparent. The client MUST be prompted before being
    authenticated to another service.
3.  method [OPTIONAL] - The method to be used when sending responses.
    See section [2.1.1](#id.86jlo2in39du) for details

#### 2.2.2. parameters for username/password authentication {.c7}

In addition to the OPTIONAL parameters specified in Section 2.2.1, the
following HTTP request parameters MUST be passed to /login while it is
acting as a credential acceptor for username/password authentication.
They are all case-sensitive.

1.  username [REQUIRED] - the username of the client that is trying to
    log in
2.  password [REQUIRED] - the password of the client that is trying to
    log in
3.  lt [REQUIRED] - a login ticket. This is provided as part of the
    login form discussed in Section 2.1.3. The login ticket itself is
    discussed in Section 3.5.
4.  rememberMe [OPTIONAL, CAS 3.0] - if this parameter is set, a
    Long-Term Ticket Granting Ticket might be created by the CAS server
    (refered to as Remember-Me support). It is subject to the CAS server
    configuration whether Long-Term Ticket Granting Tickets are
    supported or not.

[](#)[](#)

Note: When Long-Term Ticket Granting Tickets (Remember Me) are supported
by the CAS Server, security considerations MUST be taken into account.
This for example includes shared computer usage. On CAS client systems
it might be necessary to handle Remember-Me logins different. See
Section 4.1  for details.

#### 2.2.3. parameters for trust authentication {.c7}

There are no REQUIRED HTTP request parameters for trust authentication.
Trust authentication MAY be based on any aspect of the HTTP request.

#### 2.2.4. response {.c7}

One of the following responses MUST be provided by /login when it is
operating as a credential acceptor.

1.  successful login: redirect the client to the URL specified by the
    "service" parameter in a manner that will not cause the client's
    credentials to be forwarded to the service. This redirection MUST
    result in the client issuing a GET request to the service. The
    request MUST include a valid service ticket, passed as the HTTP
    request parameter, "ticket". See Appendix B for more information. If
    "service" was not specified, CAS MUST display a message notifying
    the client that it has successfully initiated a single sign-on
    session.
2.  failed login: return to /login as a credential requestor. It is
    RECOMMENDED in this case that the CAS server display an error
    message be displayed to the user describing why login failed (e.g.
    bad password, locked account, etc.), and if appropriate, provide an
    opportunity for the user to attempt to login again.

### 2.3. /logout {.c7}

/logout destroys a client's single sign-on CAS session. The
ticket-granting cookie (Section 3.6) is destroyed, and subsequent
requests to /login will not obtain service tickets until the user again
presents primary credentials (and thereby establishes a new single
sign-on session).

#### 2.3.1. parameters {.c7}

The following HTTP request parameter MAY be specified to /logout. It is
case sensitive and SHOULD be handled by /logout.

1.  url [OPTIONAL] - if "url" is specified, the URL specified by "url"
    SHOULD be on the logout page with descriptive text. For example,
    "The application you just logged out of has provided a link it would
    like you to follow. Please click here to access
    http://www.example.org."
2.  service [OPTIONAL, CAS 3.0] - if "service" is specified, the browser
    might be automatically redirected to the URL specified by "service"
    after the logout was performed by the CAS server. If redirection by
    the CAS Server is actually performed depends on the server
    configuration.

#### 2.3.2. response {.c7}

[CAS 1.0, CAS 2.0 ] /logout MUST display a page stating that the user
has been logged out. If the "url" request parameter is implemented,
/logout SHOULD also provide a link to the provided URL as described in
Section 2.3.1.

[CAS 3.0] /logout MUST display a page stating that the user has been
logged out if no "service" parameter was provided. If a "service"
request parameter with an encoded URL value is provided, the CAS server
redirects to the given service URL after successful logout.  If a "url"
request parameter is provided, /logout SHOULD display a link to the
provided URL as described in Section 2.3.1. 

[](#)[](#)

Note: When CAS Server operates in non-open mode (allowed Service URLs
are registered within the CAS Server), the CAS server MUST ensure that
only registered Service URLs are accepted for redirection ([service]
parameter) or displaying ([url] parameter)

#### 2.3.3 Single Logout {.c7}

The CAS Server MAY support Single Logout (SLO). SLO means that the user
gets logged out not only from the  CAS Server, but also from all visited
CAS client applications. If SLO is supported by the CAS Server, the CAS
Server MUST send a HTTP POST request containing a logout XML document
(see Appendix C) to all service URLs provided to CAS during this CAS
session whenever a Ticket Granting Ticket is explicitly

expired by the user (e.g. during logout). CAS Clients that do not
support the SLO POST requests MUST ignore these requests. SLO requests
MAY also be initiated by the CAS Server upon TGT idle timeout.

##### 2.3.3.1 Server behaviour {.c7}

The CAS Server SHALL ignore all errors that might occur on a Single
Logout POST request to CAS Client applications service URLs.  This is so
that CAS Client erros do not disturb CAS Server performance and
availability (“fire and forget”).

##### 2.3.3.2 Client behaviour {.c7}

If the client supports SLO POST requests, the client SHALL return a HTTP
success status code.

Handling the logout POST request data is up to the CAS client. It is
recommended to logout the user from the application identified by the
service ticket id sent in the SLO POST request.  

### 2.4. /validate [CAS 1.0] {.c7}

/validate checks the validity of a service ticket. /validate is part of
the CAS 1.0 protocol and thus does not handle proxy authentication. CAS
MUST respond with a ticket validation failure response when a proxy
ticket is passed to /validate.

#### 2.4.1. parameters {.c7}

The following HTTP request parameters MAY be specified to /validate.
They are case sensitive and MUST all be handled by /validate.

1.  service [REQUIRED] - the identifier of the service for which the
    ticket was issued, as discussed in Section 2.2.1.
2.  ticket [REQUIRED] - the service ticket issued by /login. Service
    tickets are described in Section 3.1.
3.  renew [OPTIONAL] - if this parameter is set, ticket validation will
    only succeed if the service ticket was issued from the presentation
    of the user's primary credentials. It will fail if the ticket was
    issued from a single sign-on session.

#### 2.4.2. response {.c7}

/validate will return one of the following two responses:

On ticket validation success:

yes\<LF\>\
username\<LF\>\
\

On ticket validation failure:

no\<LF\>\
\<LF\>\
\

#### 2.4.3. URL examples of /validate {.c7}

Simple validation attempt:

https://cas.example.org/cas/validate?service=http%3A%2F%2Fwww.example.org%2Fservice&ticket=ST-1856339-aA5Yuvrxzpv8Tau1cYQ7\

Ensure service ticket was issued by presentation of primary credentials:

https://cas.example.org/cas/validate?service=http%3A%2F%2Fwww.example.org%2Fservice&ticket=ST-1856339-aA5Yuvrxzpv8Tau1cYQ7&renew=true\
\

### 2.5. /serviceValidate [CAS 2.0] {.c7}

/serviceValidate checks the validity of a service ticket and returns an
XML-fragment response. /serviceValidate MUST also generate and issue
proxy-granting tickets when requested. /serviceValidate MUST NOT return
a successful authentication if it receives a proxy ticket. It is
RECOMMENDED that if /serviceValidate receives a proxy ticket, the error
message in the XML response SHOULD explain that validation failed
because a proxy ticket was passed to /serviceValidate.

#### 2.5.1. parameters {.c7}

The following HTTP request parameters MAY be specified to
/serviceValidate. They are case sensitive and MUST all be handled by
/serviceValidate.

1.  service [REQUIRED] - the identifier of the service for which the
    ticket was issued, as discussed in Section 2.2.1.
2.  ticket [REQUIRED] - the service ticket issued by /login. Service
    tickets are described in Section 3.1.
3.  pgtUrl [OPTIONAL] - the URL of the proxy callback. Discussed in
    Section 2.5.4.
4.  renew [OPTIONAL] - if this parameter is set, ticket validation will
    only succeed if the service ticket was issued from the presentation
    of the user's primary credentials. It will fail if the ticket was
    issued from a single sign-on session.

#### 2.5.2. response {.c7}

/serviceValidate will return an XML-formatted CAS serviceResponse as
described in the XML schema in Appendix A. Below are example responses:

On ticket validation success:

\<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas"\>

  \<cas:authenticationSuccess\>

    \<cas:user\>username\</cas:user\>

      
\<cas:proxyGrantingTicket\>PGTIOU-84678-8a9d...\</cas:proxyGrantingTicket\>

  \</cas:authenticationSuccess\>

\</cas:serviceResponse\>\

On ticket validation failure:

\<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas"\>

        \<cas:authenticationFailure code="INVALID\_TICKET"\>

            Ticket ST-1856339-aA5Yuvrxzpv8Tau1cYQ7 not recognized

        \</cas:authenticationFailure\>

\</cas:serviceResponse\>

For proxy responses, see section 2.6.2

#### 2.5.3. error codes {.c7}

The following values MAY be used as the "code" attribute of
authentication failure responses. The following is the minimum set of
error codes that all CAS servers MUST implement. Implementations MAY
include others.

1.  INVALID\_REQUEST - not all of the required request parameters were
    present
2.  INVALID\_TICKET - the ticket provided was not valid, or the ticket
    did not come from an initial login and "renew" was set on
    validation. The body of the
    \<[cas:authenticationFailure](http://casauthenticationfailure)\>
    block of the XML response SHOULD describe the exact details.
3.  INVALID\_SERVICE - the ticket provided was valid, but the service
    specified did not match the service associated with the ticket. CAS
    MUST invalidate the ticket and disallow future validation of that
    same ticket.
4.  INTERNAL\_ERROR - an internal error occurred during ticket
    validation

For all error codes, it is RECOMMENDED that CAS provide a more detailed
message as the body of the
\<[cas:authenticationFailure](http://casauthenticationfailure)\> block
of the XML response.

#### 2.5.4. proxy callback {.c7}

If a service wishes to proxy a client's authentication to a back-end
service, it must acquire a proxy-granting ticket. Acquisition of this
ticket is handled through a proxy callback URL. This URL will uniquely
and securely identify the service that is proxying the client's
authentication. The back-end service can then decide whether or not to
accept the credentials based on the proxying service identifying
callback URL.

The proxy callback mechanism works as follows:

1.  The service that is requesting a proxy-granting ticket specifies
    upon initial service ticket or proxy ticket validation the HTTP
    request parameter "pgtUrl" to /serviceValidate (or /proxyValidate).
    This is a callback URL of the service to which CAS will connect to
    verify the service's identity. This URL MUST be HTTPS, and CAS MUST
    verify both that the SSL certificate is valid and that its name
    matches that of the service. If the certificate fails validation, no
    proxy-granting ticket will be issued, and the CAS service response
    as described in Section 2.5.2 MUST NOT contain a
    \<proxyGrantingTicket\> block. At this point, the issuance of a
    proxy-granting ticket is halted, but service ticket validation will
    continue, returning success or failure as appropriate. If
    certificate validation is successful, issuance of a proxy-granting
    ticket proceeds as in step b.
2.  CAS uses an HTTP GET request to pass the HTTP request parameters
    "pgtId" and "pgtIou" to the pgtUrl. These entities are discussed in
    Sections 3.3 and 3.4, respectively.
3.  If the HTTP GET returns an HTTP status code of 200 (OK), CAS MUST
    respond to the /serviceValidate (or /proxyValidate) request with a
    service response (Section 2.5.2) containing the proxy-granting
    ticket IOU (Section 3.4) within the
    \<[cas:proxyGrantingTicket](http://casproxygrantingticket)\> block.
    If the HTTP GET returns any other status code, excepting HTTP 3xx
    redirects, CAS MUST respond to the /serviceValidate (or
    /proxyValidate) request with a service response that MUST NOT
    contain a
    \<[cas:proxyGrantingTicket](http://casproxygrantingticket)\> block.
    CAS MAY follow any HTTP redirects issued by the pgtUrl. However, the
    identifying callback URL provided upon validation in the \<proxy\>
    block MUST be the same URL that was initially passed to
    /serviceValidate (or /proxyValidate) as the "pgtUrl" parameter.
4.  The service, having received a proxy-granting ticket IOU in the CAS
    response, and both a proxy-granting ticket and a proxy-granting
    ticket IOU from the proxy callback, will use the proxy-granting
    ticket IOU to correlate the proxy-granting ticket with the
    validation response. The service will then use the proxy-granting
    ticket for the acquisition of proxy tickets as described in Section
    2.7.

#### 2.5.5 Attributes {.c7}

[CAS 3.0] The response document MAY include an optional
\<cas:attributes\> element for additional authentication and/or user
attributes. See Appendix A.

#### 2.5.6. URL examples of /serviceValidate {.c7}

Simple validation attempt:

https://cas.example.org/cas/serviceValidate?service=http%3A%2F%2Fwww.example.org%2Fservice&ticket=ST-1856339-aA5Yuvrxzpv8Tau1cYQ7\

Ensure service ticket was issued by presentation of primary credentials:

https://cas.example.org/cas/serviceValidate?service=http%3A%2F%2Fwww.example.org%2Fservice&ticket=ST-1856339-aA5Yuvrxzpv8Tau1cYQ7&renew=true\
\
Pass in a callback URL for proxying:

[https://cas.example.org/cas/serviceValidate?service=http%3A%2F%2Fwww.example.org%2Fservice&ticket=ST-1856339-aA5Yuvrxzpv8Tau1cYQ7&pgtUrl=https://www.example.org%2Fservice%2FproxyCallback](https://cas.example.org/cas/serviceValidate?service=http%3A%2F%2Fwww.example.org%2Fservice&ticket=ST-1856339-aA5Yuvrxzpv8Tau1cYQ7&pgtUrl=https://www.example.org%2Fservice%2FproxyCallback)

#### 2.5.7 Example response with attributes {.c7}

\<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas"\>

  \<cas:authenticationSuccess\>

    \<cas:user\>username\</cas:user\>

    \<cas:attributes\>

      \<cas:firstname\>John\</cas:firstname\>

      \<cas:lastname\>Doe\</cas:lastname\>

      \<cas:title\>Mr.\</cas:title\>

     
\<cas:email\>[jdoe@example.org](mailto:jdoe@example.org)\</cas:email\>

      \<cas:affiliation\>staff\</cas:affiliation\>

      \<cas:affiliation\>faculty\</cas:affiliation\>

    \</cas:attributes\>

   
\<cas:proxyGrantingTicket\>PGTIOU-84678-8a9d...\</cas:proxyGrantingTicket\>

  \</cas:authenticationSuccess\>

\</cas:serviceResponse\>\
\

### 2.6. /proxyValidate [CAS 2.0] {.c7}

/proxyValidate MUST perform the same validation tasks as
/serviceValidate and additionally validate proxy tickets. /proxyValidate
MUST be capable of validating both service tickets and proxy tickets.
See Section 2.5.4 for details.

#### 2.6.1. parameters {.c7}

/proxyValidate has the same parameter requirements as /serviceValidate.
See Section 2.5.1.

#### 2.6.2. response {.c7}

/proxyValidate will return an XML-formatted CAS serviceResponse as
described in the XML schema in Appendix A. Below are example responses:

On ticket validation success:

\<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'\>

  \<cas:authenticationSuccess\>

    \<cas:user\>username\</cas:user\>

   
\<cas:proxyGrantingTicket\>PGTIOU-84678-8a9d...\</cas:proxyGrantingTicket\>

    \<cas:proxies\>

      \<cas:proxy\>https://proxy2/pgtUrl\</cas:proxy\>

      \<cas:proxy\>https://proxy1/pgtUrl\</cas:proxy\>

    \</cas:proxies\>

  \</cas:authenticationSuccess\>

\</cas:serviceResponse\>

[](#)[](#)

Note: when authentication has proceeded through multiple proxies, the
order in which the proxies were traversed MUST be reflected in the
\<[cas:proxies](http://casproxies)\> block. The most recently-visited
proxy MUST be the first proxy listed, and all the other proxies MUST be
shifted down as new proxies are added. In the above example, the service
identified by <https://proxy1/pgtUrl> was visited first, and that
service proxied authentication to the service identified by
<https://proxy2/pgtUrl>.

Response on ticket validation failure:
``xml
\<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'\>\
    \<cas:authenticationFailure code="INVALID\_TICKET"\>\
        ticket PT-1856376-1HMgO86Z2ZKeByc5XdYD not recognized\
    \</cas:authenticationFailure\>\
\</cas:serviceResponse\>\
``
#### 2.6.3 error codes {.c7}

See section 2.5.3

#### 2.6.4 URL examples of /proxyValidate {.c7}

/proxyValidate accepts the same parameters as /serviceValidate. See
Section 2.5.5 for use examples, substituting "proxyValidate" for
"serviceValidate".

### 2.7. /proxy [CAS 2.0] {.c7}

/proxy provides proxy tickets to services that have acquired
proxy-granting tickets and will be proxying authentication to back-end
services.

#### 2.7.1. parameters {.c7}

The following HTTP request parameters MUST be specified to /proxy. They
are both case-sensitive.

1.  pgt [REQUIRED] - the proxy-granting ticket acquired by the service
    during service ticket or proxy ticket validation
2.  targetService [REQUIRED] - the service identifier of the back-end
    service. Note that not all back-end services are web services so
    this service identifier will not always be a URL. However, the
    service identifier specified here MUST match the "service" parameter
    specified to /proxyValidate upon validation of the proxy ticket.

#### 2.7.2. response {.c7}

/proxy will return an XML-formatted CAS serviceResponse as described in
the XML schema in Appendix A. Below are example responses:

On request success:

\<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'\>\
    \<cas:proxySuccess\>\
      
 \<cas:proxyTicket\>PT-1856392-b98xZrQN4p90ASrw96c8\</cas:proxyTicket\>\
    \</cas:proxySuccess\>\
\</cas:serviceResponse\>\

On request failure:

\<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'\>\
    \<cas:proxyFailure code="INVALID\_REQUEST"\>\
        'pgt' and 'targetService' parameters are both required\
    \</cas:proxyFailure\>\
\</cas:serviceResponse\>\
\

#### 2.7.3. error codes {.c7}

The following values MAY be used as the "code" attribute of
authentication failure responses. The following is the minimum set of
error codes that all CAS servers MUST implement. Implementations MAY
include others.

1.  INVALID\_REQUEST - not all of the required request parameters were
    present
2.  BAD\_PGT - the pgt provided was invalid
3.  INTERNAL\_ERROR - an internal error occurred during ticket
    validation

For all error codes, it is RECOMMENDED that CAS provide a more detailed
message as the body of the
\<[cas:authenticationFailure](http://casauthenticationfailure)\> block
of the XML response.

#### 2.7.4. URL example of /proxy {.c7}

Simple proxy request:

[https://server/cas/proxy?targetService=http%3A%2F%2Fwww.service.com&pgt=...](https://server/cas/proxy?targetService=http%3A%2F%2Fwww.service.com&pgt=PGT-490649-W81Y9Sa2vTM7hda7xNTkezTbVge4CUsybAr)...

3. CAS Entities {.c7}
---------------

### 3.1. service ticket {.c7}

A service ticket is an opaque string that is used by the client as a
credential to obtain access to a service. The service ticket is obtained
from CAS upon a client's presentation of credentials and a service
identifier to /login as described in Section 2.2.

#### 3.1.1. service ticket properties {.c7}

1.  Service tickets are only valid for the service identifier that was
    specified to /login when they were generated. The service identifier
    SHOULD NOT be part of the service ticket.
2.  Service tickets MUST only be valid for one ticket validation
    attempt. Whether or not validation was successful, CAS MUST then
    invalidate the ticket, causing all future validation attempts of
    that same ticket to fail.
3.  CAS SHOULD expire unvalidated service tickets in a reasonable period
    of time after they are issued. If a service presents for validation
    an expired service ticket, CAS MUST respond with a validation
    failure response. It is RECOMMENDED that the validation response
    include a descriptive message explaining why validation failed. It
    is RECOMMENDED that the duration a service ticket is valid before it
    expires be no longer than five minutes. Local security and CAS usage
    considerations MAY determine the optimal lifespan of unvalidated
    service tickets.
4.  Service tickets MUST contain adequate secure random data so that a
    ticket is not guessable.
5.  Service tickets MUST begin with the characters, "ST-".
6.  Services MUST be able to accept service tickets of up to 32
    characters in length. It is RECOMMENDED that services support
    service tickets of up to 256 characters in length.

### 3.2. proxy ticket {.c7}

A proxy ticket is an opaque string that a service uses as a credential
to obtain access to a back-end service on behalf of a client. Proxy
tickets are obtained from CAS upon a service's presentation of a valid
proxy-granting ticket (Section 3.3), and a service identifier for the
back-end service to which it is connecting.

#### 3.2.1. proxy ticket properties {.c7}

1.  Proxy tickets are only valid for the service identifier specified to
    /proxy when they were generated. The service identifier SHOULD NOT
    be part of the proxy ticket.
2.  Proxy tickets MUST only be valid for one ticket validation attempt.
    Whether or not validation was successful, CAS MUST then invalidate
    the ticket, causing all future validation attempts of that same
    ticket to fail.
3.  CAS SHOULD expire unvalidated proxy tickets in a reasonable period
    of time after they are issued. If a service presents for validation
    an expired proxy ticket, CAS MUST respond with a validation failure
    response. It is RECOMMENDED that the validation response include a
    descriptive message explaining why validation failed. It is
    RECOMMENDED that the duration a proxy ticket is valid before it
    expires be no longer than five minutes. Local security and CAS usage
    considerations MAY determine the optimal lifespan of unvalidated
    proxy tickets.
4.  Proxy tickets MUST contain adequate secure random data so that a
    ticket is not guessable.
5.  Proxy tickets SHOULD begin with the characters, "PT-". Proxy tickets
    MUST begin with either the characters, "ST-" or "PT-".
6.  Back-end services MUST be able to accept proxy tickets of up to 32
    characters in length. It is RECOMMENDED that back-end services
    support proxy tickets of up to 256 characters in length.

### 3.3. proxy-granting ticket {.c7}

A proxy-granting ticket is an opaque string that is used by a service to
obtain proxy tickets for obtaining access to a back-end service on
behalf of a client. Proxy-granting tickets are obtained from CAS upon
validation of a service ticket or a proxy ticket. Proxy-granting ticket
issuance is described fully in Section 2.5.4.

#### 3.3.1. proxy-granting ticket properties {.c7}

1.  Proxy-granting tickets MAY be used by services to obtain multiple
    proxy tickets. Proxy-granting tickets are not one-time-use tickets.
2.  Proxy-granting tickets MUST expire when the client whose
    authentication is being proxied logs out of CAS.
3.  Proxy-granting tickets MUST contain adequate secure random data so
    that a ticket is not guessable in a reasonable period of time
    through brute-force attacks.
4.  Proxy-granting tickets SHOULD begin with the characters, "PGT-".
5.  Services MUST be able to handle proxy-granting tickets of up to 64
    characters in length. It is RECOMMENDED that services support
    proxy-granting tickets of up to 256 characters in length.

### 3.4. proxy-granting ticket IOU {.c7}

A proxy-granting ticket IOU is an opaque string that is placed in the
response provided by /serviceValidate and /proxyValidate used to
correlate a service ticket or proxy ticket validation with a particular
proxy-granting ticket. See Section 2.5.4 for a full description of this
process.

#### 3.4.1. proxy-granting ticket IOU properties {.c7}

1.  Proxy-granting ticket IOUs SHOULD NOT contain any reference to their
    associated proxy-granting tickets. Given a particular PGTIOU, it
    MUST NOT be possible to derive its corresponding PGT through
    algorithmic methods in a reasonable period of time.
2.  Proxy-granting ticket IOUs MUST contain adequate secure random data
    so that a ticket is not guessable in a reasonable period of time
    through brute-force attacks.
3.  Proxy-granting ticket IOUs SHOULD begin with the characters,
    "PGTIOU-".
4.  Services MUST be able to handle PGTIOUs of up to 64 characters in
    length. It is RECOMMENDED that services support PGTIOUs of up to 256
    characters in length.

### 3.5. login ticket {.c7}

A login ticket is a string that is provided by /login as a credential
requestor and passed to /login as a credential acceptor for
username/password authentication. Its purpose is to prevent the
replaying of credentials due to bugs in web browsers.

#### 3.5.1. login ticket properties {.c7}

1.  Login tickets issued by /login MUST be probabilistically unique.
2.  Login tickets MUST only be valid for one authentication attempt.
    Whether or not authentication was successful, CAS MUST then
    invalidate the login ticket, causing all future authentication
    attempts with that instance of that login ticket to fail.
3.  Login tickets SHOULD begin with the characters, "LT-".

### 3.6. ticket-granting cookie {.c7}

A ticket-granting cookie is an HTTP
cookie[[5](http://www.ja-sig.org/products/cas/protocol.html#5)] set by
CAS upon the establishment of a single sign-on session. This cookie
maintains login state for the client, and while it is valid, the client
can present it to CAS in lieu of primary credentials. Services can opt
out of single sign-on through the "renew" parameter described in
Sections 2.1.1, 2.4.1, and 2.5.1.

#### 3.6.1. ticket-granting cookie properties {.c7}

1.  Ticket-granting cookies MUST be set to expire at the end of the
    client's browser session.
2.  CAS MUST set the cookie path to be as restrictive as possible. For
    example, if the CAS server is set up under the path /cas, the cookie
    path MUST be set to /cas.
3.  The value of ticket-granting cookies MUST contain adequate secure
    random data so that a ticket-granting cookie is not guessable in a
    reasonable period of time.
4.  The value of ticket-granting cookies SHOULD begin with the
    characters, "TGC-".

### 3.7. ticket and ticket-granting cookie character set {.c7}

In addition to the above requirements, all CAS tickets and the value of
the ticket-granting cookie MUST contain only characters from the set
{A-Z, a-z, 0-9, and the hyphen character “-”}.

4. Optional Features {.c7}
--------------------

### 4.1 Long-Term Tickets - Remember-Me [CAS 3.0] {.c7}

CAS Server MAY support Long-Term Ticket Granting Tickets (referred to as
"Remember Me" functionality). If this feature is supported by the CAS
Server, it is possible to perform recurring, non interactive re-logins
to the CAS Server as long as the Long-Term Ticket Granting Ticket in the
CAS Server is not expired and the TGC Cookie is valid.

#### 4.1.1 Enabling Remember-Me (Login Page) {.c7}

The CAS Server MUST provide a checkbox on the login page to allow
Remember-Me functionality. By default, the checkbox MUST be unchecked.
It MUST be the users choice to enable Remember-Me for the login or not.
See Section 2.2.2.

#### 4.1.2 Security implications {.c7}

Enabling Remember-Me MAY have security implications. As the CAS
authentication is bound to the browser and the user is not getting
interactively logged-in when a valid Long-Term TGT ticket exists and the
CAS cookie presented by the browser is valid, special care MUST be taken
on the CAS client side to handle Remember-Me logins properly. It MUST be
the CAS clients responsiblity to decide if and when Remember-Me CAS
logins might be handled special. See 4.1.3.

#### 4.1.3 CAS Validation Response Attributes {.c7}

As only the CAS Client MUST decide how to handle Remember-Me logins (see
4.2.1), the CAS Server MUST provide information about a Remember-Me
login to the CAS Client. This information MUST be provided by all ticket
validation methods supported by the CAS Server (See Sections 2.5, 2.6
and 2.8) in this case.

1.  in serviceValidate XML responses (see Appendix A), a Remember-Me
    login MUST be indicated by the
    longTermAuthenticationRequestTokenUsed attribute. Additionally, the
    isFromNewLogin attribute MAY be used to decide whether this has
    security implications.
2.  In SAML validation responses, Remember-Me MUST be indicated by the
    longTermAuthenticationRequestTokenUsed attribute.

#### 4.1.4 CAS Client requirements {.c7}

If the CAS client needs to special handle Remember-Me logins (e.g.
access to sensitive areas of the CAS client application), the CAS client
MUST NOT use the  /validate CAS validation URL, as this URL does not
support CAS attributes in the validation response document.

### 4.2 /samlValidate [CAS 3.0] {.c7}

/samlValidate checks the validity of a Service Ticket by a SAML 1.1
request document provided by a HTTP POST. A SAML (Secure Access Markup
Language)[7]  1.1 response document MUST be returned. This allows the
release of additional information (attributes) of the authenticated
NetID. The Security Assertion Markup Language (SAML) describes a
document and protocol framework by which security assertions (such as
assertions about a prior act of authentication) can be exchanged.

#### 4.2.1 parameters {.c7}

The following HTTP request parameters MUST be specified to
/samlValidate. They are both case-sensitive.

1.  TARGET [REQUIRED] - URL encoded service identifier of the back-end
    service. Note that as an HTTP request parameter, this URL value MUST
    be URL-encoded as described in Section 2.2 of RFC 1738[4]. The
    service identifier specified here MUST match the "service" parameter
    provided to /login. See Section 2.1.1.
2.  TODO

#### 4.2.2 HTTP Request Method and Body {.c7}

Request to /samlValidate MUST be POST requests. The request body MUST be
a valid SAML 1.0 or 1.1 request XML document of document type
"text/xml".

#### 4.2.3 SAML request values {.c7}

1.  RequestID [REQUIRED] - unique identifier for the request
2.  IssueInstant [REQUIRED] - timestamp of the request
3.  samlp:AssertionArtifact [REQUIRED] - the valid CAS Service Ticket
    obtained as a response parameter at login. See section 2.2.4.

#### 4.2.4 Example of /samlValidate POST request {.c7}

POST /cas/samlValidate?ticket=

Host: cas.example.com

Content-Length: 491

Content-Type: text/xml

\<SOAP-ENV:Envelope
xmlns:SOAP-ENV="[http://schemas.xmlsoap.org/soap/envelope/"](http://schemas.xmlsoap.org/soap/envelope/)\>

  \<SOAP-ENV:Header/\>

  \<SOAP-ENV:Body\>

    \<samlp:Request
[xmlns:samlp](http://xmlnssamlp)="[urn:oasis:names:tc:SAML:1.0:protocol]()"
MajorVersion="1"

      MinorVersion="1" RequestID="\_192.168.16.51.1024506224022"

      IssueInstant="2002-06-19T17:03:44.022Z"\>

      \<samlp:AssertionArtifact\>

        ST-1-u4hrm3td92cLxpCvrjylcas.example.com

      \</samlp:AssertionArtifact\>

    \</samlp:Request\>

  \</SOAP-ENV:Body\>

\</SOAP-ENV:Envelope\>

#### 4.2.5 SAML response {.c7}

CAS Server response to a /samlValidate request MUST be a SAML 1.1
response.

Example SAML 1.1 validation response

[](#)[](#)

\<SOAP-ENV:Envelope
xmlns:SOAP-ENV="[http://schemas.xmlsoap.org/soap/envelope/"](http://schemas.xmlsoap.org/soap/envelope/)\>

  \<SOAP-ENV:Header /\>

  \<SOAP-ENV:Body\>

    \<Response
xmlns="[urn:oasis:names:tc:SAML:1.0:protocol]()"[ ](http://xmlnssaml)[xmlns:saml](http://xmlnssaml)="[urn:oasis:names:tc:SAML:1.0:assertion]()"

   
[xmlns:samlp](http://xmlnssamlp)="[urn:oasis:names:tc:SAML:1.0:protocol]()"[ ](http://xmlnsxsd)[xmlns:xsd](http://xmlnsxsd)="[http://www.w3.org/2001/XMLSchema"](http://www.w3.org/2001/XMLSchema)

   
[xmlns:xsi](http://xmlnsxsi)="[http://www.w3.org/2001/XMLSchema-instance"](http://www.w3.org/2001/XMLSchema-instance) IssueInstant="2008-12-10T14:12:14.817Z"

    MajorVersion="1" MinorVersion="1"
Recipient="[https://eiger.iad.vt.edu/dat/home.do"](https://eiger.iad.vt.edu/dat/home.do)

    ResponseID="\_5c94b5431c540365e5a70b2874b75996"\>

      \<Status\>

        \<StatusCode Value="samlp:Success"\>

        \</StatusCode\>

      \</Status\>

      \<Assertion xmlns="[urn:oasis:names:tc:SAML:1.0:assertion]()"
AssertionID="\_e5c23ff7a3889e12fa01802a47331653"

      IssueInstant="2008-12-10T14:12:14.817Z" Issuer="localhost"
MajorVersion="1"

      MinorVersion="1"\>

        \<Conditions NotBefore="2008-12-10T14:12:14.817Z"
NotOnOrAfter="2008-12-10T14:12:44.817Z"\>

          \<AudienceRestrictionCondition\>

            \<Audience\>

             
[https://some-service.example.com/app/](https://some-service.example.com/app/)

            \</Audience\>

          \</AudienceRestrictionCondition\>

        \</Conditions\>

        \<AttributeStatement\>

          \<Subject\>

            \<NameIdentifier\>johnq\</NameIdentifier\>

            \<SubjectConfirmation\>

              \<ConfirmationMethod\>

                [urn:oasis:names:tc:SAML:1.0:cm:artifact]()

              \</ConfirmationMethod\>

            \</SubjectConfirmation\>

          \</Subject\>

          \<Attribute AttributeName="uid"
AttributeNamespace="[http://www.ja-sig.org/products/cas/"](http://www.ja-sig.org/products/cas/)\>

            \<AttributeValue\>12345\</AttributeValue\>

          \</Attribute\>

          \<Attribute AttributeName="groupMembership"
AttributeNamespace="[http://www.ja-sig.org/products/cas/"](http://www.ja-sig.org/products/cas/)\>

            \<AttributeValue\>

              uugid=middleware.staff,ou=Groups,dc=vt,dc=edu

            \</AttributeValue\>

          \</Attribute\>

          \<Attribute AttributeName="eduPersonAffiliation"
AttributeNamespace="[http://www.ja-sig.org/products/cas/"](http://www.ja-sig.org/products/cas/)\>

            \<AttributeValue\>staff\</AttributeValue\>

          \</Attribute\>

          \<Attribute AttributeName="accountState"
AttributeNamespace="[http://www.ja-sig.org/products/cas/"](http://www.ja-sig.org/products/cas/)\>

            \<AttributeValue\>ACTIVE\</AttributeValue\>

          \</Attribute\>

        \</AttributeStatement\>

        \<AuthenticationStatement
AuthenticationInstant="2008-12-10T14:12:14.741Z"

       
AuthenticationMethod="[urn:oasis:names:tc:SAML:1.0:am:password]()"\>

          \<Subject\>

            \<NameIdentifier\>johnq\</NameIdentifier\>

            \<SubjectConfirmation\>

              \<ConfirmationMethod\>

                [urn:oasis:names:tc:SAML:1.0:cm:artifact]()

              \</ConfirmationMethod\>

            \</SubjectConfirmation\>

          \</Subject\>

        \</AuthenticationStatement\>

      \</Assertion\>

    \</Response\>

  \</SOAP-ENV:Body\>

\</SOAP-ENV:Envelope\>

##### 4.2.5.1 SAML CAS response attributes {.c7}

The following additional attributes might be provided within the SAML
response:

1.  longTermAuthenticationRequestTokenUsed - If Long Term Ticket
    Granting Tickets are supported by the CAS Server (see Section 4.1),
    the SAML response MUST include this attribute to indicate this to
    the CAS client

 {.c7 .c48}

Appendix A: CAS response XML schema {.c7}
===================================

\<?xml version="1.0" encoding="UTF-8"?\>

\<!--

Licensed to Jasig under one or more contributor license

agreements. See the NOTICE file distributed with this work

for additional information regarding copyright ownership.

Jasig licenses this file to you under the Apache License,

Version 2.0 (the "License"); you may not use this file

except in compliance with the License. You may obtain a

copy of the License at the following location:

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing,

software distributed under the License is distributed on an

"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY

KIND, either express or implied. See the License for the

specific language governing permissions and limitations

under the License.

--\> 

\<!--

    Authors:

    Drew Mazurek Version: \$Id: cas2.xsd,v 1.1 2005/02/14 16:19:06
dmazurek Exp \$

    Robert Oschwald Version: 3.0

--\>

\<[xs:schema](http://xsschema) xmlns:xs="[http://www.w3.org/2001/XMLSchema](http://www.w3.org/2001/XMLSchema)" [xmlns:cas](http://xmlnscas)="[http://www.yale.edu/tp/cas](http://www.yale.edu/tp/cas)"

   
targetNamespace="[http://www.yale.edu/tp/cas](http://www.yale.edu/tp/cas)" elementFormDefault="qualified"

    attributeFormDefault="unqualified"\>

    \<[xs:annotation](http://xsannotation)\>

        \<[xs:documentation](http://xsdocumentation)\>

            The following is the schema for the Central Authentication
Service (CAS) version 3.0 protocol

            response.  

            This covers the responses for the following servlets:

                /serviceValidate,

                /proxyValidate

                /proxy

            This specification is subject to change.

        \</[xs:documentation](http://xsdocumentation)\>

    \</[xs:annotation](http://xsannotation)\>

   
\<[xs:element](http://xselement) name="serviceResponse" type="cas:ServiceResponseType"/\>

   
\<[xs:complexType](http://xscomplextype) name="ServiceResponseType"\>

        \<[xs:choice](http://xschoice)\>

           
\<[xs:element](http://xselement) name="authenticationSuccess" type="cas:AuthenticationSuccessType"/\>

           
\<[xs:element](http://xselement) name="authenticationFailure" type="cas:AuthenticationFailureType"/\>

           
\<[xs:element](http://xselement) name="proxySuccess" type="cas:ProxySuccessType"/\>

           
\<[xs:element](http://xselement) name="proxyFailure" type="cas:ProxyFailureType"/\>

        \</[xs:choice](http://xschoice)\>

    \</[xs:complexType](http://xscomplextype)\>

   
\<[xs:complexType](http://xscomplextype) name="AuthenticationSuccessType"\>

        \<[xs:sequence](http://xssequence)\>

           
\<[xs:element](http://xselement) name="user" type="[xs:string](http://xsstring)"/\>

           
\<[xs:element](http://xselement) name="attributes" type="cas:AttributesType" minOccurs="0"/\>^[[d]](#cmnt4)^

           
\<[xs:element](http://xselement) name="proxyGrantingTicket" type="[xs:string](http://xsstring)" minOccurs="0"/\>

           
\<[xs:element](http://xselement) name="proxies" type="cas:ProxiesType" minOccurs="0"/\>

        \</[xs:sequence](http://xssequence)\>

    \</[xs:complexType](http://xscomplextype)\>

    \<[xs:complexType](http://xscomplextype) name="ProxiesType"\>

        \<[xs:sequence](http://xssequence)\>

           
\<[xs:element](http://xselement) name="proxy" type="[xs:string](http://xsstring)" maxOccurs="unbounded"/\>

        \</[xs:sequence](http://xssequence)\>

    \</[xs:complexType](http://xscomplextype)\>

   
\<[xs:complexType](http://xscomplextype) name="AuthenticationFailureType"\>

        \<[xs:simpleContent](http://xssimplecontent)\>

           
\<[xs:extension](http://xsextension) base="[xs:string](http://xsstring)"\>

               
\<[xs:attribute](http://xsattribute) name="code" type="[xs:string](http://xsstring)" use="required"/\>

            \</[xs:extension](http://xsextension)\>

        \</[xs:simpleContent](http://xssimplecontent)\>

    \</[xs:complexType](http://xscomplextype)\>

    \<[xs:complexType](http://xscomplextype) name="ProxySuccessType"\>

        \<[xs:sequence](http://xssequence)\>

           
\<[xs:element](http://xselement) name="proxyTicket" type="[xs:string](http://xsstring)"/\>

        \</[xs:sequence](http://xssequence)\>

    \</[xs:complexType](http://xscomplextype)\>

    \<[xs:complexType](http://xscomplextype) name="ProxyFailureType"\>

        \<[xs:simpleContent](http://xssimplecontent)\>

           
\<[xs:extension](http://xsextension) base="[xs:string](http://xsstring)"\>

               
\<[xs:attribute](http://xsattribute) name="code" type="[xs:string](http://xsstring)" use="required"/\>

            \</[xs:extension](http://xsextension)\>

        \</[xs:simpleContent](http://xssimplecontent)\>

    \</[xs:complexType](http://xscomplextype)\>

    \<[xs:complexType](http://xscomplextype) name="AttributesType"\>

        \<[xs:sequence](http://xssequence)\>

           
\<[xs:element](http://xselement) name="authenticationDate" type="[xs:dateTime](http://xsdatetime)" minOccurs="0"/\>

           
\<[xs:element](http://xselement) name="longTermAuthenticationRequestTokenUsed" type="[xs:boolean](http://xsboolean)"

                minOccurs="0"\>

               
\<[xs:annotation](http://xsannotation)\>\<[xs:documentation](http://xsdocumentation)\>

                    true if a long-term (Remember-Me token was used)

               
\</[xs:documentation](http://xsdocumentation)\>\</[xs:annotation](http://xsannotation)\>

            \</[xs:element](http://xselement)\>

           
\<[xs:element](http://xselement) name="isFromNewLogin" type="[xs:boolean](http://xsboolean)" minOccurs="0"\>

               
\<[xs:annotation](http://xsannotation)\>\<[xs:documentation](http://xsdocumentation)\>

                    true if this was from a new, interactive login.

                    If login was from a non-interactive login (e.g.
Remember-Me), this value is false or might be omitted.

               
\</[xs:documentation](http://xsdocumentation)\>\</[xs:annotation](http://xsannotation)\>

            \</[xs:element](http://xselement)\>

           
\<[xs:element](http://xselement) name="memberOf" type="[xs:string](http://xsstring)" minOccurs="0" maxOccurs="unbounded"\>

               
\<[xs:annotation](http://xsannotation)\>\<[xs:documentation](http://xsdocumentation)\>

                    One or many elements describing the units the user
is member in.

                    E.g. LDAP format values.

               
\</[xs:documentation](http://xsdocumentation)\>\</[xs:annotation](http://xsannotation)\>

            \</[xs:element](http://xselement)\>^[[e]](#cmnt5)^

            \<xs:element name="userAttributes" minOccurs="0"\>

                     \<xs:complexType\>

                    \<xs:sequence\>

                       
\<xs:any minOccurs="0" maxOccurs="unbounded" processContents="lax"\>

                            \<xs:annotation\>

                                \<xs:documentation\>

                                    Any user specific attribute
elements.

                                \</xs:documentation\>

                            \</xs:annotation\>

                        \</xs:any\>

                    \</xs:sequence\>

                \</xs:complexType\>^[[f]](#cmnt6)^

            \</xs:element\>^[[g]](#cmnt7)^

        \</[xs:sequence](http://xssequence)\>

    \</[xs:complexType](http://xscomplextype)\>

\</[xs:schema](http://xsschema)\>

 

Note:

As userAttributes can be extended by the CAS Server implementor (see
\<xs:any\> schema definition), it is recommended to form custom
attributes as \<cas:attributes name=”NAME”\>VALUE\</cas:attributes\>.

Appendix B: Safe redirection {.c7}
----------------------------

After a successful login, safely redirecting the client from CAS to its
final destination must be handled with care. In most cases, the client
has sent credentials to the CAS server over a POST request. By this
specification, the CAS server must then forward the user to the
application with a GET request.

The HTTP/1.1 RFC[3] provides a response code of 303: See Other, which
provides for the desired behavior: a script that receives data through a
POST request can, through a 303 redirection, forward the browser to
another URL through a GET request. However, not all browsers have
implemented this behavior correctly.

The recommended method of redirection is thus JavaScript. A page
containing a window.location.href in the following manner performs
adequately:

\<html\>\
    \<head\>\
        \<title\>Yale Central Authentication Service\</title\>\
        \<script\>\
          
 window.location.href="https://portal.yale.edu/Login?ticket=ST-..."
mce\_href="https://portal.yale.edu/Login?ticket=ST-...";\
       \</script\>\
    \</head\>\
    \<body\>\
        \<noscript\>\
            \<p\>CAS login successful.\</p\>\
            \<p\>  Click \<a
xhref="https://portal.yale.edu/Login?ticket=ST-..."
mce\_href="https://portal.yale.edu/Login?ticket=ST-..."\>here\</a\>\
            to access the service you requested.\<br /\>  \</p\>\
        \</noscript\>\
    \</body\>\
\</html\>\
\
\
\

 

Additionally, CAS should disable browser caching by setting all of the
various cache-related headers:

1.  Pragma: no-cache
2.  Cache-Control: no-store
3.  Expires: [RFC 1123[6] date equal to or before now]

The introduction of the login ticket removed the possibility of CAS
accepting credentials that were cached and replayed by a browser.
However, early versions of Apple's Safari browser contained a bug where
through usage of the Back button, Safari could be coerced into
presenting the client's credentials to the service it is trying to
access. CAS can prevent this behavior by not automatically redirecting
if it detects that the remote browser is one of these early versions of
Safari. Instead, CAS should display a page that states login was
successful, and provide a link to the requested service. The client must
then manually click to proceed.

Appendix C: Logout XML document {.c7}
-------------------------------

When SLO is supported by the CAS Server, it will callback to each of the
services that are registered with the system and send a POST request
with the following SAML Logout Request XML document:

[](#)[](#)

\<samlp:LogoutRequest xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" ID="[RANDOM
ID]" Version="2.0" IssueInstant="[CURRENT DATE/TIME]"\>

 
\<saml:NameID xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"\>@NOT\_USED@\</saml:NameID\>

  \<samlp:SessionIndex\>[SESSION IDENTIFIER]\</samlp:SessionIndex\>

\</samlp:LogoutRequest\>

Appendix D: References {.c7}
----------------------

[1] Bradner, S., "Key words for use in RFCs to Indicate Requirement
Levels", [RFC 2119](http://www.ietf.org/rfc/rfc2119.txt), Harvard
University, March 1997.

[2] Berners-Lee, T., Fielding, R., Frystyk, H., "Hypertext Transfer
Protocol - HTTP/1.0", [RFC 1945](http://www.ietf.org/rfc/rfc1945.txt),
MIT/LCS, UC Irvine, MIT/LCS, May 1996.

[3] Fielding, R., Gettys, J., Mogul, J., Frystyk, H., Masinter, L.,
Leach, P., Berners-Lee, T., "Hypertext Transfer Protocol - HTTP/1.1",
[RFC 2068](http://www.ietf.org/rfc/rfc2068.txt), UC Irvine, Compaq/W3C,
Compaq, W3C/MIT, Xerox, Microsoft, W3C/MIT, June 1999.

[4] Berners-Lee, T., Masinter, L., and MaCahill, M., "Uniform Resource
Locators (URL)", [RFC 1738](http://www.ietf.org/rfc/rfc1738.txt), CERN,
Xerox Corporation, University of Minnesota, December 1994.

[5] Kristol, D., Montulli, L., "HTTP State Management Mechanism", [RFC
2965](http://www.ietf.org/rfc/rfc2965.txt), Bell Laboratories/Lucent
Technologies, Epinions.com, Inc., October 2000.

[6] Braden, R., "Requirements for Internet Hosts - Application and
Support", [RFC 1123](http://www.ietf.org/rfc/rfc1123.txt), Internet
Engineering Task Force, October 1989.

[7] OASIS SAML 1.1 standard, saml.xml.org, December 2009.

Appendix E: CAS License^[[h]](#cmnt8)^ {.c7}
--------------------------------------

Copyright 2013, JA-SIG, Inc.
This project includes software developed by Jasig.
http://www.jasig.org/
 
Licensed under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License at:
 
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.


Copyright (c) 2000-2005 Yale University. All rights reserved.

THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY
DISCLAIMED. IN NO EVENT SHALL YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED, THE COSTS OF
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.

Redistribution and use of this software in source or binary forms, with
or without modification, are permitted, provided that the following
conditions are met:

​1. Any redistribution must include the above copyright notice and
disclaimer and this list of conditions in any related documentation and,
if feasible, in the redistributed software.

​2. Any redistribution must include the acknowledgment, "This product
includes software developed by Yale University," in any related
documentation and, if feasible, in the redistributed software.

​3. The names "Yale" and "Yale University" must not be used to endorse
or promote products derived from this software.

Appendix F: Changes to this Document {.c7}
------------------------------------

May 4, 2005: v1.0 - initial release

March 2, 2012: v1.0.1 - fixed "noscropt" typo. apetro per amazurek with
credit to Faraz Khan at ASU for catching the typo.

April, 2013: v2.0 - CAS 3.0 protocol draft (work in progress)

^[[i]](#cmnt9)^

---
layout: default
title: CAS - CAS Protocol
---


# CAS protocol
The CAS protocol is a simple and powerful ticket-based protocol developed exclusively for CAS. A complete protocol specification may be found at [http://www.jasig.org/cas/protocol](http://www.jasig.org/cas/protocol).

It involves one or many clients and one server.  
Clients are embedded in *cassified* applications (called "CAS services") whereas the CAS server is a standalone component:
- the [CAS server](../installation/Configuring-Authentication-Components.html) is responsible for authenticating users and granting accesses to applications
- the [CAS clients](../integration/CAS-Clients.html) protect the CAS applications and retrieve the identity of the granted users from the CAS server.

The key concepts are:
- the TGT (Ticket Granting Ticket), stored in the CASTGC cookie, represents a SSO session for a user
- the ST (Service Ticket), transmitted as a GET parameter in urls, stands for the access granted by the CAS server to the *cassified* application for a specific user.



## Versions
The current CAS protocol is the [version 3.0](https://github.com/Jasig/cas/blob/master/cas-server-protocol/3.0/cas_protocol_3_0.md), implemented by the CAS server 4.0.  
It's mainly a capture of the most common enhancements built on top of the CAS protocol revision 2.0.  
Among all features, the most noticable update between versions 2.0 and 3.0 is the ability to return the authentication/user attributes in the `/serviceValidate` response.




## Web flow diagram

<a href="../images/cas_flow_diagram.png" target="_blank"><img src="../images/cas_flow_diagram.png" alt="CAS Web flow diagram" title="CAS Web flow diagram" /></a>




## Proxy web flow diagram
One of the most powerful feature of the CAS protocol is the ability for a CAS service to act as a proxy for another CAS service, transmitting the user identity.

<a href="../images/cas_proxy_flow_diagram.jpg" target="_blank"><img src="../images/cas_proxy_flow_diagram.jpg" alt="CAS Proxy web flow diagram" title="CAS Proxy web flow diagram" /></a>




## Other protocols
Even if the primary goal of the CAS server is to implement the CAS protocol, other protocols are also supported as extensions:
- [OpenID](../protocol/OpenID-Protocol.html)
- [OAuth](../protocol/OAuth-Protocol.html)
- [SAML](../protocol/SAML-Protocol.html)

***



#Or delegate the authentication to another CAS server
Using the CAS protocol, the CAS server can also be configured to [delegate the authentication](../integration/Delegate-Authentication.html) to another CAS server.


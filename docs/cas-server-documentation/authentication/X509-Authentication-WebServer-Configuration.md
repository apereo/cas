---
layout: default
title: CAS - X.509 Authentication
category: Authentication
---
{% include variables.html %}

# X.509 Authentication - Web Server Configuration

X.509 configuration requires substantial configuration outside the CAS Web application. The configuration of Web
server SSL components varies dramatically with software and is outside the scope of this document. We offer some
general advice for SSL configuration:

* Configuring SSL components for optional client certificate behavior generally provides better user experience.
Requiring client certificates prevents SSL negotiation in cases where the certificate is not present, which prevents
user-friendly server-side error messages.
* Accept certificates only from trusted issuers, generally those within your PKI.
* Specify all certificates in the certificate chain(s) of allowed issuers.

## Embedded Web Server

While instructions here generally apply to an external server deployment such as Apache Tomcat, that
is not a hard requirement. X.509 authentication can be achieved with an embedded Apache Tomcat
container that ships with CAS and can be potentially simplify the configuration and automation steps
quite a bit, depending on use case and behavior. The configuration of certificate and trust stores 
as well as behavior and enforcement of client authentication can also be managed directly by CAS.

{% include_cached casproperties.html thirdPartyStartsWith="server.ssl" %}

### Optional (Mixed) Authentication

When using an [embedded Apache Tomcat container](../installation/Configuring-Servlet-Container.html), it may be 
required to allow the user to select either X.509 authentication or the usual CAS login flow without first being prompted.
In this scenario, the user is allowed the option to select a login flow via X.509 at which time the browser would present
a dialog prompt asking for a certificate selection and then passing it onto CAS to proceed.

This behavior is achieved by exposing a dedicated port for the embedded Apache Tomcat container that 
may forcefully require X.509 authentication for login and access. Doing so should automatically allow for an extra
login option in the user interface to trigger the browser for X.509.

{% include_cached casproperties.html properties="cas.authn.x509.webflow" %}

## External Apache Tomcat

Anything said here extends the [Apache Tomcat reference for SSL](https://tomcat.apache.org/tomcat-11.0-doc/ssl-howto.html).

The Tomcat server is configured in `$CATALINA_HOME/conf/server.xml` with one or more `<Connector>` elements. Each of these elements 
defines one port number on which Tomcat will listen for requests. Connectors that support SSL are configured with one or two files 
that represent a collection of X.509 certificates.

- The `keystoreFile` is a collection of X.509 certificates one of which Tomcat will use to identify itself to 
browsers. This certificate contains the DNS name of the server on which Tomcat is running which the HTTP client 
will have used as the server name part of the URL. It is possible to use a file that contains multiple 
certificates (in which case Tomcat will use the certificate stored under the alias "Tomcat" or, if that 
is not found, will use the first certificate it finds that also has an associated private key). However, 
to assure that no mistakes are made it is sensible practice to use a file that has only the one host 
certificate, plus of course its private key and chain of parent Certificate Authorities.

- The `truststoreFile` is a collection of X.509 certificates representing Certificate Authorities from which 
Tomcat is willing to accept user certificates. Since the `keystoreFile` contains the CA that issued the certificate identifying the server, 
the `truststoreFile` and `keystoreFile` could be the same in a CAS configuration where the URL (actually the port) that uses X.509 authentication is 
not the well know widely recognized URL for interactive (userid/password form) login, and therefore the only CA 
that it trusts is the institutional internal CA.

One strategy if you are planning to support both X.509 and userid/password validation through the same port is to put a 
public (VeriSign, Thawte) certificate for this server in the `keystoreFile`, but then put only the institutional 
internal CA certificate in the `truststoreFile`. Logically and in all the documentation, 
the Certificate Authority that issues the certificate to the 
server which the browser trusts is completely and logically independent of the Certificate Authority that issues the 
certificate to the user which the server then trusts. Java keeps them separate, Tomcat keeps them separate, and browsers 
should not be confused if, during SSL negotiation, the server requests a user certificate from a CA other than the 
one that issued the server's own identifying certificate. In this configuration, the Server issues a public certificate 
every browser will accept and the browser is strongly urged to send only a private institutional 
certificate that can be mapped to a Principal name.

<div class="alert alert-info">:information_source: <strong>Almost There</strong><p>If you previously configured CAS without 
X.509 authentication, then you probably have the <code>keystoreFile</code> already configured and
loaded with a certificate identifying this server. All you need to add is the <code>truststoreFile</code> part.</p></div>

The configured connector will look something like:

```xml
<!-- Define a SSL HTTP/1.1 Connector on port 443 -->
<!-- if you do not specify a truststoreFile, then the default java "cacerts" truststore will be used-->
<Connector port="443"
    maxHttpHeaderSize="8192"
    maxThreads="150"
    minSpareThreads="25"
    maxSpareThreads="75"
    enableLookups="false"
    disableUploadTimeout="true"
    acceptCount="100"
    scheme="https"
    secure="true"
    clientAuth="want"
    sslProtocol="TLS"
    keystoreFile="/path/to/keystore.jks"
    keystorePass="secret"
    truststoreFile="/path/to/myTrustStore.jks"
    truststorePass="secret" />
```

The `clientAuth="want"` tells Tomcat to request that the browser provide a user certificate if one is available. If 
you want to force the use of user certificates, replace `"want"` with `"true"`.
If you specify `"want"` and the browser does not have a certificate, then CAS may forward the request to the login form.

The keystore can be in `JKS` or `PKCS12` format when using Tomcat. When using both `PKCS12` and JKS keystore types 
then you should specify the type of each keystore by using the `keystoreType` and `truststoreType` attributes.

You may import the certificate of the institutional Certificate Authority (the one that issues User certificates) using the command:

```bash
# Create a blank keystore to start from scratch if needed
# keytool -genkey -keyalg RSA -alias "selfsigned" -keystore myTrustStore.jks -storepass "secret" -validity 360
# keytool -delete -alias "selfsigned" -keystore myTrustStore.jks

keytool -import -alias myAlias -keystore /path/to/myTrustStore.jks -file certificateForInstitutionalCA.crt
```


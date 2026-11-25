# Overview

Source: https://apereo.github.io/cas/development/integration/ACME-Integration.html

Enable support for the *Automatic Certificate Management Environment* (ACME) protocol as specified in [RFC 8555](https://tools.ietf.org/html/rfc8555). ACME is a protocol that a certificate authority (CA) such as Let’s Encrypt and an applicant can use to automate the process of verification and certificate issuance.

> **Usage**
>
> **This feature is deprecated and is scheduled to be removed in the future.**

Support is enabled by including the following dependency in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-acme"
}
```

The activation of this module is performed on startup when the CAS server is ready serve http requests. When this module is finished successfully, the domain key pair composed of the key and the certificate (including the full certificate path) will be produced in separate files.

If no account was registered with the CA yet, there will also be a new key as the account key pair.

The CSR file contains the CSR that was used for the certificate order. It is generated for convenience and will not be needed later again. When the certificate is renewed, a new CSR will be generated.

There are two sets of key pairs. One is required for creating and accessing your account and the other is required for encrypting the traffic on your domain(s). It is strongly encouraged to use separate key pairs for account and each of the certificates.

> **Note**
> Backup key pairs in a safe place, as you will be locked out from your account if you should ever lose it! There is no way to recover a lost key pair, or regain access to your account when the key is lost.

## HTTP Challenge

For the HTTP challenge, CAS server will respond to `GET` requests at `/.well-known/acme-challenge/{token}` path. The request is always performed against port `80` and at the root of the domain which means CAS MUST be able to operate at that port and context path to respond to verification challenges.

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.acme.domain-chain.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.acme.domain-csr.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.acme.domain-key.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.acme.domains` (Required; type `List<String>`): List of domains or sub domains that are requesting a certificate renewal.
- `cas.acme.server-url` (Required; type `String`; default `acme://letsencrypt.org/staging`): Server url to contact, when requesting certificates. Use `acme://letsencrypt.org` for production.
- `cas.acme.terms-of-use-accepted` (Required; type `Boolean`; default `false`): Flag that indicates ACME terms of use has been accepted by the user.
- `cas.acme.user-key.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.acme.key-size` (Optional; type `Integer`; default `2048`): Indicate the key length/size used when requesting/generating keys.
- `cas.acme.retry-attempts` (Optional; type `Integer`; default `3`): Number of attempts to retry when executing certificate orders or checking for status of an existing order or challenge acknowledgement.
- `cas.acme.retry-internal` (Optional; type `String`; default `PT2S`): Delay interval between to retry attempts when executing certificate orders or checking for status of an existing order or challenge acknowledgement.

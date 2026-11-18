# Adaptive Authentication - IP Intelligence

Source: https://apereo.github.io/cas/development/mfa/Adaptive-Authentication-IP-Intelligence.html

CAS provides you with the capability to examine the client IP address and decide whether access should be granted. This may be useful to detect bot, proxy or VPN traffic and protect your deployment from fraud, automated attacks, crawlers, etc.

The result of the IP address examination may either ban and request the request, allow it to go through, or present a score to indicate the probability of an IP address that may be questionable. If the result is ranked score, it will be compared against the configured risk threshold to determine whether the request may proceed.

Banned IP address can either be defined as patterns in the CAS settings, or they may be examined using the listed strategies below.

**REST**

The client IP address is submitted to a REST endpoint as the header `clientIpAddress` under a `GET` request. The expected result status codes are the following:

| Code | Description |
| --- | --- |
| `401`, `403` | IP address is banned and the request will be rejected. |
| `200`, `202` | IP address is allowed and the request may proceed. |
| All Others | Response body is expected to contain a score between `1` and `0`, (`1=Banned` and `0=Allowed`). |

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.adaptive.ip-intel.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.adaptive.ip-intel.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.adaptive.ip-intel.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.adaptive.ip-intel.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.adaptive.ip-intel.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.adaptive.ip-intel.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

**Groovy**

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

The client IP address may be examined using a Groovy script whose outline should match the following:

```groovy
import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*
import org.apereo.cas.authentication.adaptive.intel.*

def run(Object[] args) {
    def (requestContext,clientIpAddress,logger) = args
    
    logger.info("Client ip address provided is ${clientIpAddress}")
    
    if (ipAddressIsRejected())
        return IPAddressIntelligenceResponse.banned()
    
    return IPAddressIntelligenceResponse.allowed()
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.adaptive.ip-intel.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

**BlackDot**

Please [see this link](https://getipintel.net/) for more info. A valid subscription is required for large query counts.

> **Usage Warning!**
>
> This is a free service, primarily useful for development, testing and demos. Production deployments of this service require a subscription that can handle the expected query count and load.

Note that a valid email that is checked frequently must be used in the contact field or else the service might be disabled without notice. Furthermore, **DO NOT** exceed more than 500 queries per day & 15 queries per minute. See [FAQ](https://getipintel.net/#FAQ) for further information.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.adaptive.ip-intel.black-dot.email-address` (Required; type `String`): Include your contact information so you can be notified if a problem arise or if there are core changes.
- `cas.authn.adaptive.ip-intel.black-dot.mode` (Optional; type `String`; default `DYNA_LIST`): Control the mode. - `DYNA_LIST`: If you get a value between 0 - 1, exclusive (like 0.99, 0.99999, 0.97), these values are generated by dynamic checks which looks for characteristics of the given IP. IPs that are either manually banned or seen on a public proxy site will return a value of 1. If you only want manually banned or public proxies, then in your code just look for the value "1". However, there are many IPs that haven't gone through manual review and IPs can change behavior very frequently (which is why dynamic checks exist in the first place). If you only look for the value of "1", then expect to have more proxy / VPN / bad IPs go through your system, however, false positives are less likely if you use the dynamic ban list option. If you wish to use only manually banned and public proxy IPs, append the parameter flags=m, the system will only return a result of 0 or 1. This option is the best to start off with that will have a noticeable impact in bot / proxy / VPN traffic, especially if you don't have any data sets to test with the system. - `DYNA_CHECK`: In this scenario, you want to use dynamic checks as well but you want to skip additional checks to see if the IP is a bad ip (see What do you mean by "Bad IP"?). In this mode, some bad IPs are still detected but the system does not attempt to go through the full bad IPs check because the time for the extra checks vary wildly (between an extra 200ms to 2 seconds). In this mode, false positives are more likely than dynamic ban lists only. Scores are lower compared to the full IP check (without any flag options) because less attributes are considered. If you wish to use dynamic ban list and dynamic checks only, append the parameter flags=b. This option is the best if dynamic ban lists isn't catching enough IPs but you don't want to run the full check because it takes too long and/or you want to have a predictable execution time. - `FULL`: Let the system to do a full lookup with one query.
- `cas.authn.adaptive.ip-intel.black-dot.url` (Optional; type `String`; default `http://check.getipintel.net/check.php?ip=%s`): URL endpoint of the service to make API calls.

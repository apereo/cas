# Throttling Authentication Attempts - Capacity

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Throttling-Capacity.html

CAS is able to support request rate-limiting based on the token-bucket algorithm, via the [Bucket4j](https://bucket4j.com/) project. This means that authentication requests that reach a certain configurable capacity within a time window may either be blocked or *throttled* to slow down. This is done to protect the system from overloading, allowing you to introduce a scenario to allow CAS `120` authentication requests per minute with a refill rate of `10` requests per second that would continually increase in the capacity bucket. Please note that the bucket allocation strategy is specific to the client IP address.

Enable the following module in your configuration overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-throttle-bucket4j"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.throttle.bucket4j.bandwidth` (Optional; type `List<Bucket4jBandwidthLimitProperties>`): Describe the available bandwidth and the overall limitations. Multiple bandwidths allow for different policies per unit of measure. (i.e. allows 1000 tokens per 1 minute, but not often then 50 tokens per 1 second).
- `cas.authn.throttle.bucket4j.bandwidth[0].capacity` (Optional; type `long`; default `120`): Number of tokens/requests that can be used within the time window.
- `cas.authn.throttle.bucket4j.bandwidth[0].duration` (Optional; type `String`; default `PT60S`): Time window in which capacity can be allowed.
- `cas.authn.throttle.bucket4j.bandwidth[0].initial-tokens` (Optional; type `long`): By default initial size of bucket equals to capacity. But sometimes, you may want to have lesser initial size, for example for case of cold start in order to prevent denial of service.
- `cas.authn.throttle.bucket4j.bandwidth[0].refill-count` (Optional; type `long`; default `10`): The number of tokens that should be used to refill the bucket given the specified refill duration.
- `cas.authn.throttle.bucket4j.bandwidth[0].refill-duration` (Optional; type `String`; default `PT30S`): Duration to use to refill the bucket.
- `cas.authn.throttle.bucket4j.bandwidth[0].refill-strategy` (Optional; type `Bucket4jBandwidthLimitProperties.BandwidthRefillStrategies`; default `GREEDY`): Describes how the bucket should be refilled. Specifies the speed of tokens regeneration. Available values are as follows: - `GREEDY`: This type of refill regenerates tokens in a greedy manner; it tries to add the tokens to bucket as soon as possible. For example refill "10 tokens per 1 second" adds 1 token per each 100 millisecond; in other words refill will not wait 1 second to regenerate 10 tokens. - `INTERVALLY`: This type of refill regenerates tokens in intervally manner. "Intervally" in opposite to "greedy" will wait until whole period would be elapsed before regenerating tokens.
- `cas.authn.throttle.bucket4j.blocking` (Optional; type `Boolean`; default `true`): Whether the request should block until capacity becomes available. When enabled, a request that finds the bucket empty parks its thread until the refill adds a token. On a request-serving path that turns a burst into thread-pool exhaustion, so throttling rejects the request immediately by default and only waits when this is explicitly turned on.
- `cas.authn.throttle.bucket4j.enabled` (Optional; type `Boolean`; default `true`): Decide whether bucket4j functionality should be enabled.

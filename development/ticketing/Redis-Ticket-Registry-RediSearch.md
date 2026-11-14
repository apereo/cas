# Redis Ticket Registry - Indexing & Search

Source: https://apereo.github.io/cas/development/ticketing/Redis-Ticket-Registry-RediSearch.html

For better performance, it’s best for the Redis server deployment to turn up and enable [RediSearch](https://github.com/RediSearch/RediSearch). RediSearch is a Redis module that enables querying, secondary indexing, and full-text search for Redis. These features allow CAS to build particular indexes for ticket documents for faster querying and search operations. In certain cases, this would significantly improve the performance of *lookup* operations.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-redis-modules"
}
```

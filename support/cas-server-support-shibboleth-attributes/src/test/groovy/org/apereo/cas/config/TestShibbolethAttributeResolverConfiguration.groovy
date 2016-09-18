package org.apereo.cas.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration("testShibbolethAttributeResolverConfiguration")
@PropertySource("classpath:cas-shibboleth.properties")
class TestShibbolethAttributeResolverConfiguration {
}

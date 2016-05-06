package org.apereo.cas.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.io.Resource

@Configuration("testShibbolethAttributeResolverConfiguration")
@PropertySource("classpath:cas-shibboleth.properties")
class TestShibbolethAttributeResolverConfiguration {
}

package org.apereo.cas

import io.spring.initializr.metadata.InitializrMetadataBuilder
import io.spring.initializr.metadata.InitializrMetadataProvider
import io.spring.initializr.metadata.InitializrProperties
import io.spring.initializr.metadata.SimpleInitializrMetadataProvider
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
open class CasInitializrApplication {
    @Bean
    open fun initializrMetadataProvider(properties: InitializrProperties): InitializrMetadataProvider {
        val metadata = InitializrMetadataBuilder.fromInitializrProperties(properties).build()
        return SimpleInitializrMetadataProvider(metadata)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(CasInitializrApplication::class.java, *args)
}

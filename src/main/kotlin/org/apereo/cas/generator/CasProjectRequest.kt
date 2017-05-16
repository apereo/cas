package org.apereo.cas.generator

import io.spring.initializr.generator.ProjectRequest
import io.spring.initializr.metadata.InitializrMetadata
import java.util.function.Supplier

open class CasProjectRequest(val casVersion: String) : ProjectRequest() {

    override fun initializeProperties(metadata: InitializrMetadata?) {
        super.initializeProperties(metadata)
        if ("gradle" == build) run {
            buildProperties.gradle.put("casVersion", Supplier { this.casVersion })
        }
        else {
            buildProperties.maven.put("casVersion", Supplier { this.casVersion })
        }
    }
}

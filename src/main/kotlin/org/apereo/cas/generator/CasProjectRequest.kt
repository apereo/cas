package org.apereo.cas.generator

import io.spring.initializr.generator.ProjectRequest
import io.spring.initializr.metadata.InitializrMetadata
import java.util.function.Supplier

open class CasProjectRequest(val casVersion: String) : ProjectRequest() {

    override fun initializeProperties(metadata: InitializrMetadata?) {
        super.initializeProperties(metadata)
        val props = if ("gradle" == build) {
            buildProperties.gradle
        } else {
            buildProperties.maven
        }
        props.put("casVersion", Supplier { this.casVersion })
        props.put("springBootVersion", Supplier { this.bootVersion })
    }

    override fun afterResolution(metadata: InitializrMetadata?) {
        if (!hasWebFacet()) {
            resolvedDependencies.add(metadata!!.dependencies.get("cas-server-webapp-tomcat"))
            facets.add("web")
        }
    }
}

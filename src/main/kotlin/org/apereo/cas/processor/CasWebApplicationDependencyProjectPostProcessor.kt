package org.apereo.cas.processor

import io.spring.initializr.generator.ProjectRequest
import io.spring.initializr.generator.ProjectRequestPostProcessorAdapter
import io.spring.initializr.metadata.InitializrMetadata
import org.apereo.cas.generator.CasProjectRequest
import org.springframework.stereotype.Component

@Component
open class CasWebApplicationDependencyProjectPostProcessor : ProjectRequestPostProcessorAdapter() {
    override fun postProcessBeforeResolution(request: ProjectRequest?, metadata: InitializrMetadata?) {
        super.postProcessBeforeResolution(request, metadata)
        val casRequest: CasProjectRequest = request as CasProjectRequest
        casRequest.addCasWebApplicationToProjectModel(metadata)
    }
}

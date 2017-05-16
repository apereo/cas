package org.apereo.cas.processor

import io.spring.initializr.generator.ProjectRequest
import io.spring.initializr.generator.ProjectRequestPostProcessorAdapter
import io.spring.initializr.metadata.InitializrMetadata
import org.springframework.stereotype.Component

@Component
open class CasMavenRepositoriesProjectPostProcessor : ProjectRequestPostProcessorAdapter() {
    override fun postProcessBeforeResolution(request: ProjectRequest?, metadata: InitializrMetadata?) {
        super.postProcessBeforeResolution(request, metadata)
        metadata?.configuration?.env?.repositories?.forEach { request?.repositories?.put(it.key, it.value) }
    }
}

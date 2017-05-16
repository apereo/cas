package org.apereo.cas.web

import io.spring.initializr.generator.BasicProjectRequest
import io.spring.initializr.generator.ProjectGenerator
import io.spring.initializr.metadata.DependencyMetadataProvider
import io.spring.initializr.metadata.InitializrMetadataProvider
import io.spring.initializr.util.TemplateRenderer
import io.spring.initializr.web.project.MainController
import org.apereo.cas.generator.CasProjectRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.servlet.resource.ResourceUrlProvider

open class CasMainController(metadataProvider: InitializrMetadataProvider,
                             templateRenderer: TemplateRenderer,
                             resourceUrlProvider: ResourceUrlProvider,
                             projectGenerator: ProjectGenerator,
                             dependencyMetadataProvider: DependencyMetadataProvider)
    : MainController(metadataProvider, templateRenderer, resourceUrlProvider,
        projectGenerator, dependencyMetadataProvider) {

    @Value(value = "\${casVersion}")
    lateinit var casVersion: String

    @ModelAttribute(name = "model")
    @Override
    override fun projectRequest(@RequestHeader headers: Map<String, String>): BasicProjectRequest {
        val request = CasProjectRequest(casVersion)
        request.parameters.putAll(headers)
        request.initialize(metadataProvider.get())
        return request
    }
}

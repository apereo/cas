package org.apereo.cas.generator

import io.spring.initializr.generator.ProjectRequestPostProcessor
import io.spring.initializr.generator.ProjectRequestResolver

open class CasProjectRequestResolver(postProcessors: List<ProjectRequestPostProcessor>)
    : ProjectRequestResolver(postProcessors) {

}


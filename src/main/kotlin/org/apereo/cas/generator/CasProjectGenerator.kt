package org.apereo.cas.generator

import io.spring.initializr.generator.ProjectGenerator
import io.spring.initializr.generator.ProjectRequest

open class CasProjectGenerator : ProjectGenerator() {
    init {
        setTmpdir(System.getProperty("java.io.tmpdir"))
    }

    override fun resolveModel(request: ProjectRequest?): MutableMap<String, Any> {
        val model = super.resolveModel(request)
        val casRequest: CasProjectRequest = request as CasProjectRequest

        val webapp = casRequest.getProjectCasWebApplicationDependency()
        model.put(TemplateModel.CAS_WEB_APP_DEPENDENCY, webapp)

        return model
    }
}

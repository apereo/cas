package org.apereo.cas.generator

import io.spring.initializr.generator.ProjectGenerator
import io.spring.initializr.generator.ProjectRequest
import java.io.File

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

    override fun generateGitIgnore(dir: File?, request: ProjectRequest?) {
        super.generateGitIgnore(dir, request)

        val model = resolveModel(request)
        write(File(dir, "README.md"), "README.md", model)
        write(File(dir, "LICENSE.txt"), "LICENSE.txt", model)
    }
}

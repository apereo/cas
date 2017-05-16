package org.apereo.cas.generator

import io.spring.initializr.generator.ProjectGenerator
import io.spring.initializr.generator.ProjectRequest
import org.eclipse.jgit.api.Git
import org.springframework.beans.factory.annotation.Value
import java.io.File


open class CasProjectGenerator : ProjectGenerator() {
    @Value(value = "\${initializr.artifact-id.value}")
    lateinit var artifactId: String

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

    override fun doGenerateProjectStructure(request: ProjectRequest?): File {
        val root = super.doGenerateProjectStructure(request)

        val casRequest: CasProjectRequest = request as CasProjectRequest

        val git = Git.init().setDirectory(File(root, artifactId)).call()
        git.add().addFilepattern(".").call()
        git.commit().setAll(true).setMessage("Initial project layout").call()

        if (!request.gitRemote?.isEmpty()) {
            val config = git.repository.config
            config.setString("remote", "origin", "url", request.gitRemote)
            config.save()
        }

        return root
    }

    override fun generateGitIgnore(dir: File?, request: ProjectRequest?) {
        super.generateGitIgnore(dir, request)
        val model = resolveModel(request)

        if (isMavenBuild(request)) {
            write(File(dir, "README.md"), "maven/README.md", model)
            write(File(dir, "build.cmd"), "maven/build.cmd", model)
            write(File(dir, "build.sh"), "maven/build.sh", model)
        }

        if (isGradleBuild(request)) {
            val cfg = File(dir, "cas")
            cfg.mkdirs()
            write(File(cfg, "build.gradle"), "gradle/cas/build.gradle", model)

            write(File(dir, "settings.gradle"), "gradle/settings.gradle", model)
            write(File(dir, "gradle.properties"), "gradle/gradle.properties", model)
        }

        val cfg = File(dir, "etc/cas/config")
        cfg.mkdirs()

        write(File(cfg, "application.yml"), "etc/cas/config/application.yml", model)
        write(File(cfg, "cas.properties"), "etc/cas/config/cas.properties", model)
        write(File(cfg, "log4j2.xml"), "etc/cas/config/log4j2.xml", model)

        write(File(dir, "LICENSE.txt"), "LICENSE.txt", model)
    }

    private fun isMavenBuild(request: ProjectRequest?): Boolean {
        return "maven" == request?.build
    }

    private fun isGradleBuild(request: ProjectRequest?): Boolean {
        return "gradle" == request?.build
    }
}

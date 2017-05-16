package org.apereo.cas.generator

import io.spring.initializr.generator.ProjectRequest
import io.spring.initializr.metadata.Dependency
import io.spring.initializr.metadata.InitializrMetadata
import java.util.function.Supplier

const val DEPENDENCY_CAS_SERVER_WEBAPP_TOMCAT: String = "cas-server-webapp-tomcat"
const val GROUP_ID_ORG_APEREO_CAS: String = "org.apereo.cas"

open class CasProjectRequest(var casVersion: String, var gitRemote: String) : ProjectRequest() {

    val projectModel: MutableMap<String, Any> = mutableMapOf()

    override fun initializeProperties(metadata: InitializrMetadata?) {
        super.initializeProperties(metadata)
        val props = if ("gradle" == build) {
            buildProperties.gradle
        } else {
            buildProperties.maven
        }
        props.put("cas.version", Supplier { this.casVersion })
        props.put("springboot.version", Supplier { this.bootVersion })
        props.remove("springBootVersion")
    }

    override fun afterResolution(metadata: InitializrMetadata?) {
    }

    fun containsProjectModelKey(key: String): Boolean {
        return projectModel.containsKey(key)
    }

    fun getProjectModelValue(key: String): Any? {
        return projectModel[key]
    }

    fun getProjectCasWebApplicationDependency(): String? {
        return projectModel[TemplateModel.CAS_WEB_APP_DEPENDENCY] as String?
    }

    override fun getResolvedDependencies(): MutableList<Dependency> {
        val list = super.getResolvedDependencies()
        list?.filter { it.groupId == GROUP_ID_ORG_APEREO_CAS }
            ?.onEach { it.version = "\${cas.version}" }
        return list
    }

    fun addCasWebApplicationToProjectModel(metadata: InitializrMetadata?) {
        if (super.getResolvedDependencies() == null) {
            super.setResolvedDependencies(mutableListOf())
        }

        facets.add("web")
        if (!hasFacet("webapp")) {
            projectModel.put(TemplateModel.CAS_WEB_APP_DEPENDENCY, DEPENDENCY_CAS_SERVER_WEBAPP_TOMCAT)
            dependencies.add(DEPENDENCY_CAS_SERVER_WEBAPP_TOMCAT)
            facets.add("webapp")
        } else {
            var dep = metadata!!.dependencies.all.filter { it.isStarter && it.facets.contains("webapp") }.first()
            projectModel.put(TemplateModel.CAS_WEB_APP_DEPENDENCY, dep.artifactId)
        }
    }
}

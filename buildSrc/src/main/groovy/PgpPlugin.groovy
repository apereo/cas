import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project

class PgpPluginExtension {
    Boolean enabled
}

@Slf4j
class PgpPlugin implements Plugin<Project> {
    
    void apply(Project project) {
        project.extensions.create("dependencyVerification", PgpPluginExtension)
    }
}

import groovy.util.logging.Slf4j
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.security.MessageDigest

class WitnessPluginExtension {
    List verify
    String ignorePattern
    Boolean enabled
}

@Slf4j
class WitnessPlugin implements Plugin<Project> {
    static String calculateSha256(file) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        file.eachByte 4096, { bytes, size ->
            md.update(bytes, 0, size);
        }
        return md.digest().collect { String.format "%02x", it }.join();
    }

    void apply(Project project) {
        project.extensions.create("dependencyVerification", WitnessPluginExtension)
        project.afterEvaluate {
            
            log.info "Evaluating project " + project.name

            if (!project.dependencyVerification.enabled) {
                return
            }
            
            project.configurations.compile.resolvedConfiguration.resolvedArtifacts.each {

                def groupId = it.moduleVersion.id.group
                def version = it.moduleVersion.id.version

                def artifactName = groupId + ":" + it.name + ":" + version
                def pattern = ~"$project.dependencyVerification.ignorePattern"
                log.info  "Ignore pattern is " + pattern
                if (!pattern.matcher(artifactName).find()) {

                    log.info  "Examining " + artifactName

                    def result = project.dependencyVerification.verify.find { assertion ->
                        List parts = assertion.tokenize(":")
                        String group = parts.get(0)
                        String name = parts.get(1)
                        String hash = parts.get(2)

                        log.info  "Checking dependency verification entry " + group + ":" + name

                        if (it.name.equals(name) && groupId.equals(group)) {
                            log.info  "Found hash " + hash
                            return true
                        }
                    }

                    if (result == null) {
                        throw new InvalidUserDataException("No dependency for integrity assertion found")
                    }

                    def csum = result.tokenize(":").get(2)
                    log.info  "Result is " + csum

                    def calc = calculateSha256(it.file);
                    if (!csum.equals(calc)) {
                        throw new InvalidUserDataException("Checksum failed for " + it.file
                                + " linked to dependency definition " + groupId + ":" + it.name + ":" + version
                                + ". Configured checksum is [" + csum + "] and calculated checksum is [" + calc + "]")
                    }
                }
            }
        }

        project.task('calculateChecksums') << {
            log.info  "dependencyVerification {"
            log.info  "    verify = ["

            project.configurations.compile.resolvedConfiguration.resolvedArtifacts.each {
                dep ->
                    log.info  "        '" + dep.moduleVersion.id.group + ":" + dep.name + ":" + calculateSha256(dep.file) + "',"
            }

            log.info  "    ]"
            log.info  "}"
        }
    }
}

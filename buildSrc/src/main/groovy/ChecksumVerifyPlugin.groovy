import groovy.util.logging.Slf4j
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.security.MessageDigest

class ChecksumVerifyPluginExtension {
    List verify
    String ignorePattern
}

@Slf4j
class ChecksumVerifyPlugin implements Plugin<Project> {
    static String calculateSha256(file) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        file.eachByte 4096, { bytes, size ->
            md.update(bytes, 0, size);
        }
        return md.digest().collect { String.format "%02x", it }.join();
    }


    void apply(Project project) {
        project.extensions.create("dependencyVerification", ChecksumVerifyPluginExtension)

        project.task('verifyChecksums') << {
            log.info "Evaluating project " + project.name

            project.configurations.compile.resolvedConfiguration.resolvedArtifacts.each {

                def groupId = it.moduleVersion.id.group
                def version = it.moduleVersion.id.version

                if (!version.endsWith("SNAPSHOT")) {
                    def artifactName = groupId + ":" + it.name + ":" + version
                    def pattern = ~"$project.dependencyVerification.ignorePattern"
                    log.info "Ignore pattern is " + pattern
                    if (!pattern.matcher(artifactName).find()) {

                        log.info "Examining " + artifactName

                        def result = project.dependencyVerification.verify.find { assertion ->
                            List parts = assertion.tokenize(":")
                            String group = parts.get(0)
                            String name = parts.get(1)
                            String ver = parts.get(2)
                            String hash = parts.get(3)

                            log.info "Checking dependency " + artifactName

                            if (it.name.equals(name) && groupId.equals(group) && version.equals(ver)) {
                                log.info "Found hash " + hash
                                return true
                            }
                        }

                        def calc = calculateSha256(it.file)
                        if (result == null) {
                            throw new InvalidUserDataException("No dependency configuration found in the checksums configuration for ["
                                    + artifactName + "]. Examine the pre-configured checksums and ensure ["
                                    + artifactName + "] is defined with the appropriate hash value: "
                                    + "[" + calc + "]: \n\n\t'" + artifactName + ":" + calc + "',")
                        }

                        def csum = result.tokenize(":").get(3)
                        log.info "Result is " + csum


                        if (!csum.equals(calc)) {
                            throw new InvalidUserDataException("Checksum failed for " + it.file
                                    + " linked to dependency definition " + groupId + ":" + it.name + ":" + version
                                    + ". Configured checksum is [" + csum + "] and calculated checksum is [" + calc + "]")
                        }
                    }
                }
            }
        }
    }
}

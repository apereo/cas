package org.apereo.cas.gradle

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Provider
import org.gradle.plugins.signing.SigningExtension

import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Matcher

final class CasBuildSettings {
    private static final Map<String, List<String>> INCLUDED_PROJECT_PATHS = new ConcurrentHashMap<>()
    private static final Map<String, Boolean> JAVADOC_ENABLED = new ConcurrentHashMap<>()
    private static final Map<String, Map<String, Object>> MODULE_METADATA = new ConcurrentHashMap<>()
    private static Object libraries
    private static final List<String> EXCLUDED_FILES_FROM_TEST_COVERAGE = [
        '**/docs/**',
        '**/org/springframework/**',
        '**/soap/generated/**',
        '**/net/jradius/**',
        '**/com/yubico/**',
        '**/saml/sts/SamlToken**',
        '**/**ConfigurationMetadata**',
        '**/**NimbusOAuthJacksonModule**',
        '**/**Application**',
        '**/**Application$**',
        '**/**Exception$**',
        '**/**Properties**',
        '**/**Properties$**'
    ].asImmutable()

    private CasBuildSettings() {
    }

    /**
     * Look up a build setting, first as a system property ({@code -Dname=value}) and then as an environment
     * variable named after it in upper snake case, so {@code generateGitProperties} may also be supplied as
     * {@code GENERATE_GIT_PROPERTIES}. The system property wins when both are defined. An environment variable
     * with a blank value counts as undefined, while {@code -Dname} with no value is defined and empty.
     *
     * @param project the project whose providers track the lookup as a build input
     * @param name the system property name of the setting
     * @return the provider of the setting value
     */
    static Provider<String> buildProperty(final Project project, final String name) {
        project.providers.systemProperty(name)
            .orElse(project.providers.environmentVariable(environmentVariableName(name)).filter { value -> !value.isBlank() })
    }

    /**
     * Translate a build setting name into its environment variable name, i.e. camel case to upper snake case.
     * Names that are already upper case, such as {@code PTS_ENABLED}, are returned unchanged.
     *
     * @param name the system property name of the setting
     * @return the environment variable name
     */
    static String environmentVariableName(final String name) {
        name.replaceAll(/([a-z0-9])([A-Z])/, '$1_$2').toUpperCase(Locale.ROOT)
    }

    /**
     * Read a boolean build setting. An undefined setting yields the given default; a setting defined with an empty
     * value ({@code -Dname}) or with {@code true} in any case is on, and any other value is off.
     *
     * @param project the project whose providers track the lookup as a build input
     * @param name the system property name of the setting
     * @param defaultValue the value used when the setting is not defined
     * @return whether the setting is on
     */
    static boolean isFlagEnabled(final Project project, final String name, final boolean defaultValue = false) {
        buildProperty(project, name)
            .map { value -> value.isBlank() || value.equalsIgnoreCase('true') }
            .getOrElse(defaultValue)
    }

    /**
     * Read a build setting as a string. A setting defined with a blank value yields the default.
     *
     * @param project the project whose providers track the lookup as a build input
     * @param name the system property name of the setting
     * @param defaultValue the value used when the setting is not defined or blank
     * @return the setting value, or the default
     */
    static String stringProperty(final Project project, final String name, final String defaultValue = null) {
        def value = buildProperty(project, name).getOrNull()
        value != null && !value.isBlank() ? value : defaultValue
    }

    static boolean isCi(final Project project) {
        buildProperty(project, 'CI').present
    }

    static boolean isSnapshotVersion(final Project project) {
        project.version.toString().endsWith('-SNAPSHOT')
    }

    static boolean isPublishSnapshots(final Project project) {
        isFlagEnabled(project, 'publishSnapshots') && isSnapshotVersion(project)
    }

    static boolean isPublishReleases(final Project project) {
        isFlagEnabled(project, 'publishReleases') && !isSnapshotVersion(project)
    }

    static boolean isPublishFlag(final Project project) {
        isPublishSnapshots(project) || isPublishReleases(project)
    }

    static boolean isPublishMinimalArtifacts(final Project project) {
        isFlagEnabled(project, 'publishMinimalArtifacts')
    }

    static String publishingType(final Project project) {
        stringProperty(project, 'publishingType', 'AUTOMATIC')
    }

    static boolean isSkipBootifulArtifact(final Project project) {
        isFlagEnabled(project, 'skipBootifulArtifact')
    }

    static boolean isSkipErrorProneCompiler(final Project project) {
        isFlagEnabled(project, 'skipErrorProneCompiler')
    }

    static boolean isSkipSpringBootDevTools(final Project project) {
        isFlagEnabled(project, 'skipSpringBootDevTools')
    }

    static boolean isSkipArtifactSigning(final Project project) {
        isFlagEnabled(project, 'skipArtifactSigning')
    }

    static boolean isTerminateCompilerOnWarning(final Project project) {
        isFlagEnabled(project, 'terminateCompilerOnWarning', true)
    }

    static boolean isSkipNullAway(final Project project) {
        isFlagEnabled(project, 'skipNullAway')
    }

    static boolean isSkipHotSwapAgent(final Project project) {
        isFlagEnabled(project, 'skipHotSwapAgent')
    }

    static boolean isEnableRemoteDebugging(final Project project) {
        isFlagEnabled(project, 'enableRemoteDebugging')
    }

    static String projectGroup(final Project project) {
        project.providers.gradleProperty('group').get()
    }

    static String projectVersion(final Project project) {
        project.providers.gradleProperty('version').get()
    }

    static String remoteDebuggingSuspend(final Project project) {
        isFlagEnabled(project, 'remoteDebuggingSuspend') ? 'y' : 'n'
    }

    static boolean isGenerateGitProperties(final Project project) {
        isPublishFlag(project) || isFlagEnabled(project, 'generateGitProperties')
    }

    static boolean isGenerateTimestamps(final Project project) {
        isPublishFlag(project) || isFlagEnabled(project, 'generateTimestamps')
    }

    static String repositoryUsername(final Project project) {
        buildProperty(project, 'repositoryUsername')
            .orElse(project.providers.environmentVariable('REPOSITORY_USER'))
            .getOrNull()
    }

    static String repositoryPassword(final Project project) {
        buildProperty(project, 'repositoryPassword')
            .orElse(project.providers.environmentVariable('REPOSITORY_PWD'))
            .getOrNull()
    }

    /**
     * The {@code mongoimport} executable used to upload module metadata; {@code ci/docs/publish.sh} names the same
     * setting {@code CAS_MODULE_METADATA_MONGOIMPORT_EXECUTABLE}, which is accepted as well.
     *
     * @param project the project being configured
     * @return the executable
     */
    static String mongoImportExecutable(final Project project) {
        def publishScriptSetting = project.providers.environmentVariable('CAS_MODULE_METADATA_MONGOIMPORT_EXECUTABLE').getOrNull()
        stringProperty(project, 'casModuleMetadataMongoImportExecutable',
            publishScriptSetting?.trim() ? publishScriptSetting : 'mongoimport')
    }

    static boolean isPrivateRelease(final Project project) {
        isPublishFlag(project) && isFlagEnabled(project, 'privateRelease')
    }

    static boolean isIgnoreJavadocFailures(final Project project) {
        isFlagEnabled(project, 'ignoreJavadocFailures')
    }

    /**
     * Javadoc errors fail the build unless they are explicitly ignored or the build is publishing artifacts.
     *
     * @param project the project being configured
     * @return whether javadoc tasks should fail on errors
     */
    static boolean isFailOnJavadocErrors(final Project project) {
        !isPublishFlag(project) && !isIgnoreJavadocFailures(project)
    }

    /**
     * Predictive test selection is on unless turned off via {@code PTS_ENABLED}; it never applies to releases.
     *
     * @param project the project being configured
     * @return whether predictive test selection may be used
     */
    static boolean isPtsEnabled(final Project project) {
        isFlagEnabled(project, 'PTS_ENABLED', true) && !isPublishReleases(project)
    }

    /**
     * Resolve the {@code casModules} build setting, a comma-separated list of module names without their
     * {@code cas-server-[core|support]-} prefix, into the paths of the matching projects other than the given one.
     *
     * @param project the project that pulls the modules in
     * @return the matching project paths
     */
    static List<String> casModuleProjectPaths(final Project project) {
        stringProperty(project, 'casModules', '')
            .tokenize(',')
            .collect { moduleName -> moduleName.trim() }
            .findAll { moduleName -> !moduleName.isEmpty() }
            .collectMany { moduleName -> matchingProjectPaths(project, moduleName) }
            .findAll { projectPath -> projectPath != project.path }
            .unique()
    }

    /**
     * Require signatures for releases unless signing is skipped, and sign with the in-memory PGP key from
     * {@code PGP_PRIVATE_KEY} and {@code PGP_PASSPHRASE} when both are available.
     *
     * @param project the project being configured
     * @param signing the signing extension of the project
     */
    static void configureSigning(final Project project, final SigningExtension signing) {
        signing.required = isPublishReleases(project) && !isSkipArtifactSigning(project)
        def privateKey = project.providers.environmentVariable('PGP_PRIVATE_KEY').getOrNull()
        def passphrase = project.providers.environmentVariable('PGP_PASSPHRASE').getOrNull()
        if (privateKey && passphrase) {
            signing.useInMemoryPgpKeys(privateKey, passphrase)
        }
    }

    static List<String> excludedFilesFromTestCoverage() {
        EXCLUDED_FILES_FROM_TEST_COVERAGE
    }

    static void setLibraries(final Object librariesMap) {
        libraries = librariesMap
    }

    static Object getLibraries() {
        libraries
    }

    static void configureProjectExtensions(final Project project) {
        project.ext.set('ci', isCi(project))
        project.ext.set('repositoryUsername', repositoryUsername(project))
        project.ext.set('repositoryPassword', repositoryPassword(project))
        project.ext.set('snapshotVersion', isSnapshotVersion(project))
        project.ext.set('publishSnapshots', isPublishSnapshots(project))
        project.ext.set('publishReleases', isPublishReleases(project))
        project.ext.set('publishFlag', isPublishFlag(project))
        project.ext.set('publishMinimalArtifacts', isPublishMinimalArtifacts(project))
        project.ext.set('skipBootifulArtifact', isSkipBootifulArtifact(project))
        project.ext.set('skipErrorProneCompiler', isSkipErrorProneCompiler(project))
        project.ext.set('skipSpringBootDevTools', isSkipSpringBootDevTools(project))
        project.ext.set('skipArtifactSigning', isSkipArtifactSigning(project))
        project.ext.set('terminateCompilerOnWarning', isTerminateCompilerOnWarning(project))
        project.ext.set('skipNullAway', isSkipNullAway(project))
        project.ext.set('skipHotSwapAgent', isSkipHotSwapAgent(project))
        project.ext.set('enableRemoteDebugging', isEnableRemoteDebugging(project))
        project.ext.set('remoteDebuggingSuspend', remoteDebuggingSuspend(project))
        project.ext.set('generateGitProperties', isGenerateGitProperties(project))
        project.ext.set('generateTimestamps', isGenerateTimestamps(project))
        project.ext.set('excludedFilesFromTestCoverage', excludedFilesFromTestCoverage())
    }

    static boolean isJavadocRequested(final Gradle gradle, final Project project) {
        def excluded = gradle.startParameter.excludedTaskNames
        !excluded.contains("javadoc") && !excluded.contains(":javadoc")
                && (isTaskRequested(project, 'javadoc')
                || isTaskRequested(project, 'aggregateJavadocsIntoJar'))
    }

    static List<String> includedProjectPaths(final Project project) {
        INCLUDED_PROJECT_PATHS.computeIfAbsent(project.rootDir.absolutePath) {
            def settingsFile = new File(project.rootDir, 'settings.gradle')
            if (!settingsFile.exists()) {
                return []
            }
            def projectPaths = []
            def includePattern = ~/(?:^|\s)include\s+(.+)$/
            def projectPattern = ~/["']([^"']+)["']/
            settingsFile.eachLine { line ->
                def includeMatcher = line =~ includePattern
                if (includeMatcher.find()) {
                    def includeArgs = includeMatcher.group(1)
                    Matcher projectMatcher = projectPattern.matcher(includeArgs)
                    while (projectMatcher.find()) {
                        projectPaths << ":${projectMatcher.group(1)}"
                    }
                }
            }
            projectPaths.asImmutable()
        }
    }

    static String projectNameFromPath(final String projectPath) {
        projectPath.tokenize(':').last()
    }

    static List<String> matchingProjectPaths(final Project project, final String moduleName) {
        def pattern = ~/cas-server-${moduleName}|cas-server-core-${moduleName}|cas-server-support-${moduleName}/
        includedProjectPaths(project).findAll { projectPath ->
            projectNameFromPath(projectPath).matches(pattern)
        }
    }

    static File projectDirectory(final Project project, final String projectPath) {
        new File(project.rootDir, projectPath.tokenize(':').join(File.separator))
    }

    static File projectBuildFile(final Project project, final String projectPath) {
        new File(projectDirectory(project, projectPath), 'build.gradle')
    }

    static boolean isJavadocEnabled(final Project project, final String projectPath) {
        JAVADOC_ENABLED.computeIfAbsent("${project.rootDir.absolutePath}:${projectPath}".toString()) {
            def buildFile = projectBuildFile(project, projectPath)
            if (!buildFile.exists()) {
                return true
            }
            def contents = buildFile.getText('UTF-8')
            def disabledMatcher = contents =~ /(?s)\bjavadoc\s*\{.*?\benabled\s*=\s*false\b.*?\}/
            !disabledMatcher.find()
        }
    }

    static Map<String, Object> projectModuleMetadata(final Project project, final String projectPath) {
        MODULE_METADATA.computeIfAbsent("${project.rootDir.absolutePath}:${projectPath}".toString()) {
            def buildFile = projectBuildFile(project, projectPath)
            if (!buildFile.exists()) {
                return [publishMetadata: false, description: projectNameFromPath(projectPath), projectMetadata: [:]].asImmutable()
            }

            def contents = buildFile.getText('UTF-8')
            def descriptionMatcher = contents =~ /(?m)^\s*description\s*=\s*["']([^"']+)["']/
            def publishMetadataMatcher = contents =~ /(?m)^\s*publishMetadata\s*=\s*true\s*$/
            def description = descriptionMatcher.find() ? descriptionMatcher.group(1) : projectNameFromPath(projectPath)
            def projectMetadata = extractProjectMetadata(contents)
            [
                publishMetadata: publishMetadataMatcher.find(),
                description    : description,
                projectMetadata: projectMetadata
            ].asImmutable()
        }
    }

    private static Map<String, Object> extractProjectMetadata(final String contents) {
        def marker = 'projectMetadata = ['
        def assignmentStart = contents.indexOf(marker)
        if (assignmentStart < 0) {
            return [:]
        }

        def bracketStart = contents.indexOf('[', assignmentStart)
        if (bracketStart < 0) {
            return [:]
        }

        def bracketEnd = findMatchingBracket(contents, bracketStart)
        if (bracketEnd < 0) {
            return [:]
        }

        def expression = contents.substring(bracketStart, bracketEnd + 1)
        def metadata = new GroovyShell().evaluate(expression)
        metadata instanceof Map ? new LinkedHashMap<>(metadata as Map<String, Object>).asImmutable() : [:]
    }

    private static int findMatchingBracket(final String contents, final int bracketStart) {
        def depth = 0
        boolean inSingleQuote = false
        boolean inDoubleQuote = false
        boolean escaping = false
        for (int index = bracketStart; index < contents.length(); index++) {
            def currentChar = contents.substring(index, index + 1)
            if (escaping) {
                escaping = false
            } else if (currentChar == '\\') {
                escaping = true
            } else if (currentChar == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote
            } else if (currentChar == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote
            } else if (!inSingleQuote && !inDoubleQuote) {
                if (currentChar == '[') {
                    depth++
                } else if (currentChar == ']') {
                    depth--
                    if (depth == 0) {
                        return index
                    }
                }
            }
        }
        -1
    }

    static boolean shouldRegisterRootTask(final Project project, final String taskName) {
        project == project.rootProject && !project.tasks.names.contains(taskName)
    }

    static boolean isTaskRequested(final Project project, final String taskName) {
        project.gradle.startParameter.taskNames.any { it == taskName || it.endsWith(":${taskName}") }
    }
}

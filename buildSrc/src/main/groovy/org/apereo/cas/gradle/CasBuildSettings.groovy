package org.apereo.cas.gradle

import org.gradle.api.Project

import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Matcher

final class CasBuildSettings {
    private static final String ARCH_ADJUSTED_KEY = 'build.archAdjusted'
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

    static boolean isCi(final Project project) {
        System.getenv('CI') != null || project.providers.systemProperty('CI').present
    }

    static boolean isSnapshotVersion(final Project project) {
        project.version.toString().endsWith('-SNAPSHOT')
    }

    static boolean isPublishSnapshots(final Project project) {
        project.providers.systemProperty('publishSnapshots').present && isSnapshotVersion(project)
    }

    static boolean isPublishReleases(final Project project) {
        project.providers.systemProperty('publishReleases').present && !isSnapshotVersion(project)
    }

    static boolean isPublishFlag(final Project project) {
        isPublishSnapshots(project) || isPublishReleases(project)
    }

    static boolean isPublishMinimalArtifacts(final Project project) {
        project.providers.systemProperty('publishMinimalArtifacts').present
    }

    static boolean isSkipBootifulArtifact(final Project project) {
        project.providers.systemProperty('skipBootifulArtifact').present
    }

    static boolean isSkipErrorProneCompiler(final Project project) {
        project.providers.systemProperty('skipErrorProneCompiler').present
    }

    static boolean isSkipSpringBootDevTools(final Project project) {
        project.providers.systemProperty('skipSpringBootDevTools').present
    }

    static boolean isSkipArtifactSigning(final Project project) {
        project.providers.systemProperty('skipArtifactSigning').present
    }

    static boolean isTerminateCompilerOnWarning(final Project project) {
        project.providers.systemProperty('terminateCompilerOnWarning').getOrElse('true') == 'true'
    }

    static boolean isSkipNullAway(final Project project) {
        project.providers.systemProperty('skipNullAway').getOrElse('false') == 'true'
    }

    static boolean isSkipHotSwapAgent(final Project project) {
        project.providers.systemProperty('skipHotSwapAgent').getOrElse('false') == 'true'
    }

    static boolean isEnableRemoteDebugging(final Project project) {
        project.providers.systemProperty('enableRemoteDebugging').present
    }

    static String projectGroup(final Project project) {
        project.providers.gradleProperty('group').get()
    }

    static String projectVersion(final Project project) {
        project.providers.gradleProperty('version').get()
    }

    static String remoteDebuggingSuspend(final Project project) {
        project.providers.systemProperty('remoteDebuggingSuspend').getOrElse('false') == 'true' ? 'y' : 'n'
    }

    static boolean isGenerateGitProperties(final Project project) {
        isPublishFlag(project) || project.providers.systemProperty('generateGitProperties').present
    }

    static boolean isGenerateTimestamps(final Project project) {
        isPublishFlag(project) || project.providers.systemProperty('generateTimestamps').present
    }

    static String repositoryUsername(final Project project) {
        project.providers.systemProperty('repositoryUsername').getOrElse(System.getenv('REPOSITORY_USER'))
    }

    static String repositoryPassword(final Project project) {
        project.providers.systemProperty('repositoryPassword').getOrElse(System.getenv('REPOSITORY_PWD'))
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

    static boolean isArchAdjusted() {
        System.getProperty(ARCH_ADJUSTED_KEY) == 'true'
    }

    static void markArchAdjusted() {
        System.setProperty(ARCH_ADJUSTED_KEY, 'true')
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








package org.apereo.cas.gradle

import groovy.json.JsonOutput
import groovy.xml.XmlSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.util.GradleVersion

import java.util.regex.Pattern

/**
 * Root-build dependency update report that avoids cross-project model access.
 */
abstract class CasDependencyUpdatesTask extends DefaultTask {
    private static final Pattern STABLE_VERSION = ~/^[0-9,.v-]+(-r)?$/

    @Input
    abstract ListProperty<String> getDependencyCoordinates()

    @Input
    abstract ListProperty<String> getRepositoryUrls()

    @Input
    abstract Property<String> getRevision()

    @Input
    abstract Property<String> getGradleReleaseChannel()

    @Input
    abstract Property<String> getReportfileName()

    @Input
    abstract Property<String> getOutputFormatter()

    @OutputDirectory
    abstract DirectoryProperty getReportOutputDirectory()

    CasDependencyUpdatesTask() {
        group = 'Help'
        description = 'Displays dependency updates for the root buildscript classpath.'
        outputs.upToDateWhen { false }
    }

    @TaskAction
    void dependencyUpdates() {
        def coordinates = dependencyCoordinates.get()
                .collect { Coordinate.parse(it) }
                .findAll { it != null }
                .unique { coordinate -> coordinate.key }
        def repositories = repositoryUrls.get()
                .collect { normalizeRepositoryUrl(it) }
                .findAll { it }
                .unique()

        def statuses = coordinates.collect { coordinate -> resolve(coordinate, repositories) }
        def current = statuses.findAll { it.status == 'current' }.sort()
        def outdated = statuses.findAll { it.status == 'outdated' }.sort()
        def exceeded = statuses.findAll { it.status == 'exceeded' }.sort()
        def unresolved = statuses.findAll { it.status == 'unresolved' }.sort()

        def outputDirectory = reportOutputDirectory.get().asFile
        outputDirectory.mkdirs()

        def formatters = outputFormatter.get()
                .split(',')
                .collect { it.trim().toLowerCase(Locale.ENGLISH) }
                .findAll { it }
                .toSet()
        def reportName = reportfileName.get()
        if (formatters.contains('json')) {
            writeJsonReport(new File(outputDirectory, "${reportName}.json"), current, outdated, exceeded, unresolved)
        }
        if (formatters.contains('plain') || formatters.contains('text') || formatters.isEmpty()) {
            writePlainReport(new File(outputDirectory, "${reportName}.txt"), current, outdated, exceeded, unresolved)
        }
    }

    DependencyStatus resolve(final Coordinate coordinate, final List<String> repositories) {
        def versions = repositories
                .collectMany { repository -> fetchVersions(repository, coordinate) }
                .unique()
                .findAll { candidate -> acceptsRevision(candidate) && !isRejected(coordinate.group, coordinate.name, candidate) }
        if (versions.isEmpty()) {
            return DependencyStatus.unresolved(coordinate)
        }
        def latest = versions.max { first, second -> compareVersions(first, second) }
        def comparison = compareVersions(coordinate.version, latest)
        if (comparison < 0) {
            return DependencyStatus.outdated(coordinate, latest)
        }
        if (comparison > 0) {
            return DependencyStatus.exceeded(coordinate, latest)
        }
        DependencyStatus.current(coordinate, latest)
    }

    List<String> fetchVersions(final String repositoryUrl, final Coordinate coordinate) {
        def metadataUrl = new URL("${repositoryUrl}${coordinate.group.replace('.', '/')}/${coordinate.name}/maven-metadata.xml")
        HttpURLConnection connection = null
        try {
            connection = metadataUrl.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty('User-Agent', 'Apereo CAS dependencyUpdates')
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return []
            }
            def metadata = new XmlSlurper(false, false).parse(connection.inputStream)
            metadata.versioning.versions.version.collect { it.text() }.findAll { it }
        } catch (final Exception ignored) {
            []
        } finally {
            connection?.disconnect()
        }
    }

    boolean acceptsRevision(final String version) {
        switch (revision.get().toLowerCase(Locale.ENGLISH)) {
            case 'integration':
                return true
            case 'milestone':
                return !version.toLowerCase(Locale.ENGLISH).contains('snapshot')
            case 'release':
            default:
                return !isNonStable(version)
        }
    }

    static boolean isRejected(final String group, final String name, final String version) {
        (version == '20040616' && group == 'commons-collections' && name == 'commons-collections') ||
                (group == 'com.h2database' && name == 'h2') ||
                (group == 'software.amazon.awssdk' && !version.endsWith('0'))
    }

    static boolean isNonStable(final String version) {
        def stableKeyword = ['RELEASE', 'FINAL', 'GA'].any {
            version.toUpperCase(Locale.ENGLISH).contains(it)
        }
        !stableKeyword && !(version ==~ STABLE_VERSION)
    }

    static String normalizeRepositoryUrl(final String repositoryUrl) {
        if (!repositoryUrl) {
            return null
        }
        repositoryUrl.endsWith('/') ? repositoryUrl : "${repositoryUrl}/"
    }

    static int compareVersions(final String first, final String second) {
        if (first == second) {
            return 0
        }
        def left = tokenize(first)
        def right = tokenize(second)
        def length = Math.max(left.size(), right.size())
        for (int i = 0; i < length; i++) {
            def leftToken = i < left.size() ? left[i] : 0
            def rightToken = i < right.size() ? right[i] : 0
            def comparison = compareToken(leftToken, rightToken)
            if (comparison != 0) {
                return comparison
            }
        }
        0
    }

    static List<Object> tokenize(final String version) {
        version.toLowerCase(Locale.ENGLISH)
                .replaceAll(/([a-z])(\d)/, '$1.$2')
                .replaceAll(/(\d)([a-z])/, '$1.$2')
                .split(/[.\-+_]/)
                .findAll { it }
                .collect { token -> token.isInteger() ? token.toInteger() : token }
    }

    static int compareToken(final Object first, final Object second) {
        if (first instanceof Integer && second instanceof Integer) {
            return first <=> second
        }
        if (first instanceof Integer) {
            return first == 0 ? compareQualifier('', second.toString()) : 1
        }
        if (second instanceof Integer) {
            return second == 0 ? compareQualifier(first.toString(), '') : -1
        }
        compareQualifier(first.toString(), second.toString())
    }

    static int compareQualifier(final String first, final String second) {
        qualifierRank(first) <=> qualifierRank(second) ?: first <=> second
    }

    static int qualifierRank(final String qualifier) {
        switch (qualifier) {
            case '':
            case 'ga':
            case 'final':
            case 'release':
                return 5
            case 'rc':
            case 'cr':
                return 4
            case 'm':
            case 'milestone':
                return 3
            case 'beta':
            case 'b':
                return 2
            case 'alpha':
            case 'a':
                return 1
            case 'snapshot':
                return 0
            default:
                return 0
        }
    }

    void writePlainReport(final File reportFile, final List<DependencyStatus> current,
                          final List<DependencyStatus> outdated, final List<DependencyStatus> exceeded,
                          final List<DependencyStatus> unresolved) {
        def lines = []
        lines << ''
        lines << '------------------------------------------------------------'
        lines << ': Project Dependency Updates'
        lines << '------------------------------------------------------------'
        lines << ''
        appendSection(lines, 'The following dependencies are using the latest release version:', current) {
            " - ${it.coordinate.group}:${it.coordinate.name}:${it.coordinate.version}"
        }
        appendSection(lines, 'The following dependencies have later release versions:', outdated) {
            " - ${it.coordinate.group}:${it.coordinate.name} [${it.coordinate.version} -> ${it.latestVersion}]"
        }
        appendSection(lines, 'The following dependencies exceed the version found at the release revision level:', exceeded) {
            " - ${it.coordinate.group}:${it.coordinate.name} [${it.coordinate.version} <- ${it.latestVersion}]"
        }
        appendSection(lines, 'The following dependencies could not be resolved:', unresolved) {
            " - ${it.coordinate.group}:${it.coordinate.name}:${it.coordinate.version}"
        }
        lines << 'Gradle current updates:'
        def gradleStatus = gradleStatus()
        lines << " - Gradle: [${GradleVersion.current().version}: ${gradleStatus.current.isUpdateAvailable ? gradleStatus.current.version : 'UP-TO-DATE'}]"
        lines << ''
        reportFile.text = lines.join(System.lineSeparator())
        println "Generated report file ${reportFile.path}"
        println reportFile.text
    }

    static void appendSection(final List<String> lines, final String title, final List<DependencyStatus> values,
                              final Closure<String> formatter) {
        if (!values.isEmpty()) {
            lines << title
            values.each { lines << formatter(it) }
            lines << ''
        }
    }

    void writeJsonReport(final File reportFile, final List<DependencyStatus> current,
                         final List<DependencyStatus> outdated, final List<DependencyStatus> exceeded,
                         final List<DependencyStatus> unresolved) {
        def report = [
                count     : current.size() + outdated.size() + exceeded.size() + unresolved.size(),
                current   : [count: current.size(), dependencies: current.collect { dependencyMap(it) }],
                outdated  : [count: outdated.size(), dependencies: outdated.collect { dependencyMap(it, [latest: it.latestVersion]) }],
                exceeded  : [count: exceeded.size(), dependencies: exceeded.collect { dependencyMap(it, [latest: it.latestVersion]) }],
                undeclared: [count: 0, dependencies: []],
                unresolved: [count: unresolved.size(), dependencies: unresolved.collect { dependencyMap(it) }],
                gradle    : gradleStatus()
        ]
        reportFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(report))
        println "Generated report file ${reportFile.path}"
    }

    Map<String, Object> gradleStatus() {
        def current = fetchGradleVersion('https://services.gradle.org/versions/current')
        [
                enabled         : true,
                running         : [
                        isFailure        : false,
                        isUpdateAvailable: false,
                        reason           : '',
                        version          : GradleVersion.current().version
                ],
                current         : [
                        isFailure        : false,
                        isUpdateAvailable: current ? compareVersions(GradleVersion.current().version, current) < 0 : false,
                        reason           : '',
                        version          : current ?: ''
                ],
                releaseCandidate: [
                        isFailure        : false,
                        isUpdateAvailable: false,
                        reason           : gradleReleaseChannel.get() == 'release-candidate' ? '' : 'update check disabled',
                        version          : ''
                ],
                nightly         : [
                        isFailure        : false,
                        isUpdateAvailable: false,
                        reason           : gradleReleaseChannel.get() == 'nightly' ? '' : 'update check disabled',
                        version          : ''
                ]
        ]
    }

    static String fetchGradleVersion(final String versionsUrl) {
        HttpURLConnection connection = null
        try {
            connection = new URL(versionsUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty('User-Agent', 'Apereo CAS dependencyUpdates')
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }
            def matcher = connection.inputStream.getText('UTF-8') =~ /"version"\s*:\s*"([^"]+)"/
            matcher.find() ? matcher.group(1) : null
        } catch (final Exception ignored) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    static Map<String, Object> dependencyMap(final DependencyStatus status, final Map<String, Object> extra = [:]) {
        [
                group     : status.coordinate.group,
                name      : status.coordinate.name,
                version   : status.coordinate.version,
                projectUrl: null,
                userReason: null
        ] + extra
    }

    private static final class Coordinate {
        final String group
        final String name
        final String version
        final String key

        private Coordinate(final String group, final String name, final String version) {
            this.group = group
            this.name = name
            this.version = version
            this.key = "${group}:${name}"
        }

        static Coordinate parse(final String coordinate) {
            def tokens = coordinate.split(':', 3)
            tokens.length == 3 && tokens.every { it } ? new Coordinate(tokens[0], tokens[1], tokens[2]) : null
        }
    }

    private static final class DependencyStatus implements Comparable<DependencyStatus> {
        final Coordinate coordinate
        final String latestVersion
        final String status

        private DependencyStatus(final Coordinate coordinate, final String latestVersion, final String status) {
            this.coordinate = coordinate
            this.latestVersion = latestVersion
            this.status = status
        }

        static DependencyStatus current(final Coordinate coordinate, final String latestVersion) {
            new DependencyStatus(coordinate, latestVersion, 'current')
        }

        static DependencyStatus outdated(final Coordinate coordinate, final String latestVersion) {
            new DependencyStatus(coordinate, latestVersion, 'outdated')
        }

        static DependencyStatus exceeded(final Coordinate coordinate, final String latestVersion) {
            new DependencyStatus(coordinate, latestVersion, 'exceeded')
        }

        static DependencyStatus unresolved(final Coordinate coordinate) {
            new DependencyStatus(coordinate, null, 'unresolved')
        }

        @Override
        int compareTo(final DependencyStatus other) {
            coordinate.key <=> other.coordinate.key
        }
    }
}

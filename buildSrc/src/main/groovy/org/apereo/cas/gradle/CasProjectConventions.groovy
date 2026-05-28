package org.apereo.cas.gradle

import org.gradle.api.Project

final class CasProjectConventions {
    private static final Set<String> EXCLUDED_PUBLISH_PROJECTS = [
        'api',
        'core',
        'docs',
        'support',
        'webapp',
        'cas-server-documentation',
        'cas-server-documentation-processor',
        'cas-server-support-shell'
    ] as Set<String>

    private CasProjectConventions() {
    }

    static boolean shouldBePublished(final Project project) {
        project.logger.info("Checking if project ${project.name} should be published")
        def result = !EXCLUDED_PUBLISH_PROJECTS.contains(project.name)
        project.logger.info("Project ${project.name} should${result ? '' : ' not'} be published")
        result
    }

    static boolean requiresLombok(final Project project) {
        !['api', 'core', 'docs', 'support', 'webapp'].contains(project.name) && project.name != 'cas-server-documentation'
    }
}


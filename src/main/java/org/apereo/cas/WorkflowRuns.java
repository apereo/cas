package org.apereo.cas;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum WorkflowRuns {
    CODEQL("CodeQL"),
    CODE_ANALYSIS("Code Analysis"),
    FUNCTIONAL_TESTS("Functional Tests"),
    UNIT_TESTS("Unit & Integration Tests"),
    PUBLISH_DOCS("Publish Documentation"),
    RERUN_WORKFLOWS("Rerun Workflow Runs"),
    DEPENDENCY_SUBMISSION_GRADLE("Automatic Dependency Submission (Gradle)"),
    DEPENDENCY_SUBMISSION_MAVEN("Automatic Dependency Submission (Maven)"),
    VALIDATION("Validation");

    private final String name;

    public static boolean isAnyOf(final String name) {
        return Arrays.stream(WorkflowRuns.values()).anyMatch(run -> run.getName().equalsIgnoreCase(name));
    }
}

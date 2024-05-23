package org.apereo.cas;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum WorkflowRuns {
    CODE_ANALYSIS("Code Analysis"),
    FUNCTIONAL_TESTS("Functional Tests"),
    UNIT_TESTS("Unit & Integration Tests"),
    PUBLISH_DOCS("Publish Documentation"),
    RERUN_WORKFLOWS("Rerun Workflow Runs"),
    VALIDATION("Validation");

    private final String name;

    public static boolean isAnyOf(final String name) {
        return Arrays.stream(WorkflowRuns.values()).anyMatch(run -> run.getName().equalsIgnoreCase(name));
    }
}

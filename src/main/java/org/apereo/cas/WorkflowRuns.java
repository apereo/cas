package org.apereo.cas;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum WorkflowRuns {
    CODE_ANALYSIS("Code Analysis"),
    FUNCTIONAL_TESTS("Functional Tests"),
    UNIT_TESTS("Unit & Integration Tests"),
    VALIDATION("Validation");

    private final String name;
}

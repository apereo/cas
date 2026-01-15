package org.apereo.cas.grouper;

import module java.base;
import lombok.Builder;
import lombok.Getter;

/**
 * This is {@link GrouperPermissionAssignmentsQuery}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Builder
@Getter
public class GrouperPermissionAssignmentsQuery {
    private final String attributeDefinitionName;
    private final String roleName;
    private final String roleUuid;
    private final String subjectAttributeName;
    private final String subjectId;
    private final String subjectIdentifier;
    private final String subjectSourceId;
}

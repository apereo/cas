package org.apereo.cas.grouper;

import module java.base;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

/**
 * This is {@link GrouperFacade} that acts as a wrapper
 * in front of the grouper API.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface GrouperFacade {

    /**
     * Gets permission assignments.
     *
     * @param query the query
     * @return the permission assignments
     */
    WsGetPermissionAssignmentsResults getPermissionAssignments(GrouperPermissionAssignmentsQuery query);

    /**
     * Construct grouper group attribute.
     * This is the name of every individual group attribute
     * transformed into a CAS attribute value.
     *
     * @param groupField the group field
     * @param group      the group
     * @return the final attribute name
     */
    static String getGrouperGroupAttribute(final GrouperGroupField groupField, final WsGroup group) {
        return switch (groupField) {
            case DISPLAY_EXTENSION -> group.getDisplayExtension();
            case DISPLAY_NAME -> group.getDisplayName();
            case EXTENSION -> group.getExtension();
            case NAME -> group.getName();
        };
    }

    /**
     * Gets groups for subject id.
     *
     * @param subjectId the principal
     * @return the groups for subject id
     */
    Collection<WsGetGroupsResult> getGroupsForSubjectId(String subjectId);
}

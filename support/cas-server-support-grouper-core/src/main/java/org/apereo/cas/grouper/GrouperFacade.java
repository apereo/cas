package org.apereo.cas.grouper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

import java.util.Collection;

/**
 * This is {@link GrouperFacade} that acts as a wrapper
 * in front of the grouper API.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface GrouperFacade {

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
        switch (groupField) {
            case DISPLAY_EXTENSION:
                return group.getDisplayExtension();
            case DISPLAY_NAME:
                return group.getDisplayName();
            case EXTENSION:
                return group.getExtension();
            case NAME:
            default:
                return group.getName();
        }
    }

    /**
     * Gets groups for subject id.
     *
     * @param subjectId the principal
     * @return the groups for subject id
     */
    Collection<WsGetGroupsResult> getGroupsForSubjectId(String subjectId);
}

package org.apereo.cas.grouper;

import org.apereo.cas.util.CollectionUtils;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link GrouperFacade} that acts as a wrapper
 * in front of the grouper API.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class GrouperFacade {

    /**
     * Construct grouper group attribute.
     * This is the name of every individual group attribute
     * transformed into a CAS attribute value.
     *
     * @param groupField the group field
     * @param group      the group
     * @return the final attribute name
     */
    public static String getGrouperGroupAttribute(final GrouperGroupField groupField, final WsGroup group) {
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
    public Collection<WsGetGroupsResult> getGroupsForSubjectId(final String subjectId) {
        try {
            val groupsClient = new GcGetGroups().addSubjectId(subjectId);
            val results = groupsClient.execute().getResults();
            if (results == null || results.length == 0) {
                LOGGER.warn("Subject id [{}] could not be located.", subjectId);
                return new ArrayList<>(0);
            }
            LOGGER.debug("Found [{}] groups for [{}]", results.length, subjectId);
            return CollectionUtils.wrapList(results);
        } catch (final Exception e) {
            LOGGER.warn("Grouper WS did not respond successfully. Ensure your credentials are correct "
                + ", the url endpoint for Grouper WS is correctly configured and the subject [{}] exists in Grouper.", subjectId, e);
        }
        return new ArrayList<>(0);
    }
}

package org.apereo.cas.grouper;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link GrouperFacade} that acts as a wrapper
 * in front of the grouper API.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GrouperFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrouperFacade.class);

    protected GrouperFacade() {}

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
    public static Collection<WsGetGroupsResult> getGroupsForSubjectId(final String subjectId) {
        try {
            final GcGetGroups groupsClient = new GcGetGroups().addSubjectId(subjectId);
            final WsGetGroupsResult[] results = groupsClient.execute().getResults();

            if (results == null || results.length == 0) {
                LOGGER.warn("Subject id [{}] could not be located.", subjectId);
                return new ArrayList<>(0);
            }
            LOGGER.debug("Found [{}] groups for [{}]", results.length, subjectId);
            return CollectionUtils.wrapList(results);
        } catch (final Exception e) {
            LOGGER.warn("Grouper WS did not respond successfully. Ensure your credentials are correct "
                    + ", the url endpoint for Grouper WS is correctly configured and the subject [{}]"
                    + "  exists in Grouper.", subjectId, e);
        }
        return new ArrayList<>(0);
    }
}

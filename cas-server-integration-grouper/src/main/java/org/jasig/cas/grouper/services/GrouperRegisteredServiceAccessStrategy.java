package org.jasig.cas.grouper.services;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import org.jasig.cas.services.TimeBasedRegisteredServiceAccessStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link GrouperRegisteredServiceAccessStrategy} is an access strategy
 * that consults a grouper instance to figure out affiliations associated
 * with a user.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GrouperRegisteredServiceAccessStrategy extends TimeBasedRegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -3557247044344135788L;
    private static final String GROUPER_GROUPS_ATTRIBUTE_NAME = "grouperAttributes";

    /**
     * The enum Grouper group field.
     */
    public enum GrouperGroupField {
        /**
         * Name grouper group field.
         */
        NAME,
        /**
         * Extension grouper group field.
         */
        EXTENSION,
        /**
         * Display name grouper group field.
         */
        DISPLAY_NAME,
        /**
         * Display extension grouper group field.
         */
        DISPLAY_EXTENSION
    }

    private GrouperGroupField groupField = GrouperGroupField.NAME;

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> principalAttributes) {
        final Map<String, Object> allAttributes = new HashMap<>(principalAttributes);
        final List<String> grouperGroups = new ArrayList<>();

        final WsGetGroupsResult[] results;

        try {
            final GcGetGroups groupsClient = new GcGetGroups().addSubjectId(principal);
            results = groupsClient.execute().getResults();
        } catch (final Exception e) {
            logger.warn("Grouper WS did not respond successfully. Ensure your credentials are correct "
                    + ", the url endpoint for Grouper WS is correctly configured and the subject " + principal
                    + "  exists in Grouper.", e);
            return false;
        }

        if (results == null || results.length == 0) {
            logger.warn("Subject id [{}] could not be located. Access denied", principal);
            return false;
        }

        for (final WsGetGroupsResult groupsResult : results) {
            if (groupsResult.getWsGroups() == null || groupsResult.getWsGroups().length == 0) {
                logger.warn("No groups could be found for subject [{}]. Access denied", groupsResult.getWsSubject().getName());
                return false;
            }

            for (final WsGroup group : groupsResult.getWsGroups()) {
                final String groupName = constructGrouperGroupAttribute(group);
                logger.debug("Found group name [{}] for [{}]", groupName, principal);
                grouperGroups.add(groupName);
            }
        }
        logger.debug("Adding [{}] under attribute name [{}] to collection of CAS attributes",
                grouperGroups, GROUPER_GROUPS_ATTRIBUTE_NAME);

        allAttributes.put(GROUPER_GROUPS_ATTRIBUTE_NAME, grouperGroups);
        return super.doPrincipalAttributesAllowServiceAccess(principal, allAttributes);
    }

    public void setGroupField(final GrouperGroupField groupField) {
        this.groupField = groupField;
    }

    public GrouperGroupField getGroupField() {
        return groupField;
    }

    /**
     * Construct grouper group attribute.
     * This is the name of every individual group attribute
     * transformed into a CAS attribute value.
     * @param group the group
     * @return the final attribute name
     */
    protected String constructGrouperGroupAttribute(final WsGroup group) {
        switch (this.groupField) {
            case DISPLAY_EXTENSION:
                return group.getDisplayExtension();
            case DISPLAY_NAME:
                return group.getDisplayName();
            case EXTENSION:
                return group.getExtension();
            default:
                return group.getName();
        }
    }

}

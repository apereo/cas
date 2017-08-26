package org.apereo.cas.grouper.services;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.grouper.GrouperGroupField;
import org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(GrouperRegisteredServiceAccessStrategy.class);

    private GrouperGroupField groupField = GrouperGroupField.NAME;

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> principalAttributes) {
        final Map<String, Object> allAttributes = new HashMap<>(principalAttributes);
        final List<String> grouperGroups = new ArrayList<>();

        final Collection<WsGetGroupsResult> results = GrouperFacade.getGroupsForSubjectId(principal);

        if (results.isEmpty()) {
            LOGGER.warn("Subject id [{}] could not be located. Access denied", principal);
            return false;
        }

        final boolean denied = results.stream().anyMatch(groupsResult -> {
            if (groupsResult.getWsGroups() == null || groupsResult.getWsGroups().length == 0) {
                LOGGER.warn("No groups could be found for subject [{}]. Access denied", groupsResult.getWsSubject().getName());
                return true;
            }

            Arrays.stream(groupsResult.getWsGroups()).forEach(group -> grouperGroups.add(GrouperFacade.getGrouperGroupAttribute(this.groupField, group)));
            return false;
        });

        if (denied) {
            return false;
        }

        LOGGER.debug("Adding [{}] under attribute name [{}] to collection of CAS attributes", grouperGroups, GROUPER_GROUPS_ATTRIBUTE_NAME);

        allAttributes.put(GROUPER_GROUPS_ATTRIBUTE_NAME, grouperGroups);
        return super.doPrincipalAttributesAllowServiceAccess(principal, allAttributes);
    }

    public void setGroupField(final GrouperGroupField groupField) {
        this.groupField = groupField;
    }

    public GrouperGroupField getGroupField() {
        return this.groupField;
    }
}

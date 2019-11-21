package org.apereo.cas.grouper.services;

import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.grouper.GrouperGroupField;
import org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link GrouperRegisteredServiceAccessStrategy} is an access strategy
 * that consults a grouper instance to figure out affiliations associated
 * with a user.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class GrouperRegisteredServiceAccessStrategy extends TimeBasedRegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -3557247044344135788L;

    private static final String GROUPER_GROUPS_ATTRIBUTE_NAME = "grouperAttributes";

    private GrouperGroupField groupField = GrouperGroupField.NAME;

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> principalAttributes) {
        val allAttributes = new HashMap<String, Object>(principalAttributes);
        val facade = new GrouperFacade();
        val results = facade.getGroupsForSubjectId(principal);
        val grouperGroups = new ArrayList<String>(results.size());
        if (results.isEmpty()) {
            LOGGER.warn("Subject id [{}] could not be located. Access denied", principal);
            return false;
        }
        val denied = results
            .stream()
            .anyMatch(groupsResult -> {
                if (groupsResult.getWsGroups() == null || groupsResult.getWsGroups().length == 0) {
                    LOGGER.warn("No groups could be found for subject [{}]. Access denied", groupsResult.getWsSubject().getName());
                    return true;
                }
                Arrays.stream(groupsResult.getWsGroups())
                    .forEach(group -> grouperGroups.add(GrouperFacade.getGrouperGroupAttribute(this.groupField, group)));
                return false;
            });
        if (denied) {
            return false;
        }
        LOGGER.debug("Adding [{}] under attribute name [{}] to collection of CAS attributes", grouperGroups, GROUPER_GROUPS_ATTRIBUTE_NAME);
        allAttributes.put(GROUPER_GROUPS_ATTRIBUTE_NAME, grouperGroups);
        return super.doPrincipalAttributesAllowServiceAccess(principal, allAttributes);
    }
}

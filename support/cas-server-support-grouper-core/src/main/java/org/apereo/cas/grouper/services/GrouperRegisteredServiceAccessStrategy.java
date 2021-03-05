package org.apereo.cas.grouper.services;

import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.grouper.GrouperGroupField;
import org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    /**
     * The attribute name that collects grouper groups as attributes.
     */
    public static final String GROUPER_GROUPS_ATTRIBUTE_NAME = "grouperAttributes";

    private static final long serialVersionUID = -3557247044344135788L;

    private GrouperGroupField groupField = GrouperGroupField.NAME;

    private Map<String, String> configProperties = new TreeMap<>();

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> principalAttributes) {
        val allAttributes = new HashMap<>(principalAttributes);
        val results = fetchWsGetGroupsResults(principal);
        if (results.isEmpty()) {
            LOGGER.warn("No groups could be found for [{}]", principal);
            return false;
        }
        val grouperGroups = new ArrayList<String>(results.size());
        results
            .stream()
            .filter(groupsResult -> groupsResult.getWsGroups() != null && groupsResult.getWsGroups().length > 0)
            .map(wsGetGroupsResult -> Arrays.stream(wsGetGroupsResult.getWsGroups()).collect(Collectors.toList()))
            .flatMap(List::stream)
            .forEach(group -> grouperGroups.add(GrouperFacade.getGrouperGroupAttribute(this.groupField, group)));
        LOGGER.debug("Adding [{}] under attribute name [{}] to collection of attributes", grouperGroups, GROUPER_GROUPS_ATTRIBUTE_NAME);
        allAttributes.put(GROUPER_GROUPS_ATTRIBUTE_NAME, grouperGroups);
        return super.doPrincipalAttributesAllowServiceAccess(principal, allAttributes);
    }

    /**
     * Fetch ws get groups results.
     *
     * @param principal the principal
     * @return the collection
     */
    @Synchronized
    protected Collection<WsGetGroupsResult> fetchWsGetGroupsResults(final String principal) {
        if (!this.configProperties.isEmpty()) {
            GrouperClientConfig.retrieveConfig().propertiesThreadLocalOverrideMap().putAll(this.configProperties);
        }
        return new GrouperFacade().getGroupsForSubjectId(principal);
    }
}

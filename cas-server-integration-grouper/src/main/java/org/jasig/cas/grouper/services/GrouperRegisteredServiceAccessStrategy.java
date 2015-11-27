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

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> principalAttributes) {
        final Map<String, Object> allAttributes = new HashMap<>(principalAttributes);
        final List<String> grouperGroups = new ArrayList<>();

        final GcGetGroups groupsClient = new GcGetGroups().addSubjectId(principal);
        for (final WsGetGroupsResult groupsResult : groupsClient.execute().getResults()) {
            for (final WsGroup group : groupsResult.getWsGroups()) {
                grouperGroups.add(constructGrouperGroupAttribute(group));
            }
        }
        allAttributes.put(constructGrouperGroupsAttribute(), grouperGroups);
        return super.doPrincipalAttributesAllowServiceAccess(principal, allAttributes);
    }

    /**
     * Construct grouper group attribute.
     * This is the name of every individual group attribute
     * transformed into a CAS attribute value.
     * @param group the group
     * @return the final attribute name
     */
    protected String constructGrouperGroupAttribute(final WsGroup group) {
        return group.getName();
    }

    /**
     * Construct grouper groups attribute.
     * This is the name of the attribute that CAS can use
     * to decide whether access is allowed.
     * @return the attribute name
     */
    protected String constructGrouperGroupsAttribute() {
        return "grouperAttributes";
    }
}

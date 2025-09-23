package org.apereo.cas.grouper;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetPermissionAssignments;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link DefaultGrouperFacade} that acts as a wrapper
 * in front of the grouper API.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class DefaultGrouperFacade implements GrouperFacade {

    @Override
    public Collection<WsGetGroupsResult> getGroupsForSubjectId(final String subjectId) {
        try {
            val results = fetchGroupsFor(subjectId);
            if (results == null || results.length == 0) {
                LOGGER.warn("Subject id [{}] could not be located.", subjectId);
                return new ArrayList<>();
            }
            LOGGER.debug("Found [{}] groups for [{}]", results.length, subjectId);
            return CollectionUtils.wrapList(results);
        } catch (final Exception e) {
            LOGGER.warn("Grouper WS did not respond successfully. Ensure your credentials are correct "
                + ", the url endpoint for Grouper WS is correctly configured and the subject [{}] exists in Grouper.", subjectId, e);
        }
        return new ArrayList<>();
    }

    protected WsGetGroupsResult[] fetchGroupsFor(final String subjectId) {
        val groupsClient = new GcGetGroups().addSubjectId(subjectId);
        return groupsClient.execute().getResults();
    }

    @Override
    public WsGetPermissionAssignmentsResults getPermissionAssignments(final GrouperPermissionAssignmentsQuery query) {
        val gcGetPermissionAssignments = new GcGetPermissionAssignments();
        FunctionUtils.doIfNotBlank(query.getAttributeDefinitionName(), gcGetPermissionAssignments::addAttributeDefName);
        FunctionUtils.doIfNotBlank(query.getRoleName(), gcGetPermissionAssignments::addAttributeDefName);
        FunctionUtils.doIfNotBlank(query.getRoleUuid(), gcGetPermissionAssignments::addRoleUuid);
        FunctionUtils.doIfNotBlank(query.getSubjectAttributeName(), gcGetPermissionAssignments::addSubjectAttributeName);

        if (StringUtils.isNotBlank(query.getSubjectId()) || StringUtils.isNotBlank(query.getSubjectIdentifier())) {
            val wsSubjectLookup = new WsSubjectLookup();
            wsSubjectLookup.setSubjectId(query.getSubjectId());
            wsSubjectLookup.setSubjectIdentifier(query.getSubjectIdentifier());
            wsSubjectLookup.setSubjectSourceId(query.getSubjectSourceId());
            gcGetPermissionAssignments.addSubjectLookup(wsSubjectLookup);
        }

        return gcGetPermissionAssignments.execute();
    }
}

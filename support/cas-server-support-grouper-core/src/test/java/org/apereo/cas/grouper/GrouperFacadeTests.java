package org.apereo.cas.grouper;

import module java.base;
import org.apereo.cas.util.MockWebServer;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GrouperFacadeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Grouper")
class GrouperFacadeTests {
    @Test
    void verifyAttributes() {
        val group = new WsGroup();
        group.setExtension("GroupExtension");
        group.setDisplayName("DisplayNameGroupExtension");
        group.setDisplayExtension("DisplaySampleGroupExtension");
        group.setDescription("Group Desc");
        group.setName("SampleGroup");
        group.setUuid(UUID.randomUUID().toString());
        assertNotNull(GrouperFacade.getGrouperGroupAttribute(GrouperGroupField.DISPLAY_EXTENSION, group));
        assertNotNull(GrouperFacade.getGrouperGroupAttribute(GrouperGroupField.DISPLAY_NAME, group));
        assertNotNull(GrouperFacade.getGrouperGroupAttribute(GrouperGroupField.EXTENSION, group));
        assertNotNull(GrouperFacade.getGrouperGroupAttribute(GrouperGroupField.NAME, group));
    }

    @Test
    void verifyGroups() throws Throwable {
        val facade = new DefaultGrouperFacade() {
            @Override
            public WsGetGroupsResult[] fetchGroupsFor(final String subjectId) {
                val group = new WsGroup();
                group.setExtension("GroupExtension");
                group.setDisplayName("DisplayNameGroupExtension");
                group.setDisplayExtension("DisplaySampleGroupExtension");
                group.setDescription("Group Desc");
                group.setName("SampleGroup");
                group.setUuid(UUID.randomUUID().toString());

                val result = new WsGetGroupsResult();
                result.setWsGroups(new WsGroup[]{group});
                return new WsGetGroupsResult[]{result};
            }
        };
        assertFalse(facade.getGroupsForSubjectId("casuser").isEmpty());
    }

    @Test
    void verifyGroupsFails() {
        val facade = new DefaultGrouperFacade();
        assertThrows(RuntimeException.class, () -> facade.fetchGroupsFor("casuser"));
    }

    @Test
    void verifyEmptyGroups() throws Throwable {
        val facade = new DefaultGrouperFacade() {
            @Override
            public WsGetGroupsResult[] fetchGroupsFor(final String subjectId) {
                return null;
            }
        };
        assertTrue(facade.getGroupsForSubjectId("casuser").isEmpty());
    }

    @Test
    void verifyFailedGroups() throws Throwable {
        val facade = new DefaultGrouperFacade() {
            @Override
            public WsGetGroupsResult[] fetchGroupsFor(final String subjectId) {
                throw new RuntimeException("BadGroups");
            }
        };
        assertTrue(facade.getGroupsForSubjectId("casuser").isEmpty());
    }

    @Test
    void verifyPermissionAssignments() {
        val facade = new DefaultGrouperFacade();
        var body = "{ \"" + WsGetPermissionAssignmentsResults.class.getSimpleName() + "\": {}}";
        try (val webServer = new MockWebServer(8080,
            body, Map.of("X-Grouper-success", "T", "X-Grouper-resultCode", "200"), HttpStatus.OK)) {
            webServer.start();
            val assignments = facade.getPermissionAssignments(GrouperPermissionAssignmentsQuery.builder()
                .subjectId("casuser")
                .build());
            assertNotNull(assignments);
            assertNull(assignments.getWsPermissionAssigns());
        }
    }
}


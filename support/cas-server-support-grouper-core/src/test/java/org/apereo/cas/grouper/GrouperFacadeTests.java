package org.apereo.cas.grouper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GrouperFacadeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
public class GrouperFacadeTests {
    @Test
    public void verifyAttributes() {
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
    public void verifyGroups() {
        val facade = new GrouperFacade() {
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
    public void verifyGroupsFails() {
        val facade = new GrouperFacade();
        assertThrows(RuntimeException.class, () -> facade.fetchGroupsFor("casuser"));
    }

    @Test
    public void verifyEmptyGroups() {
        val facade = new GrouperFacade() {
            @Override
            public WsGetGroupsResult[] fetchGroupsFor(final String subjectId) {
                return null;
            }
        };
        assertTrue(facade.getGroupsForSubjectId("casuser").isEmpty());
    }

    @Test
    public void verifyFailedGroups() {
        val facade = new GrouperFacade() {
            @Override
            public WsGetGroupsResult[] fetchGroupsFor(final String subjectId) {
                throw new RuntimeException("BadGroups");
            }
        };
        assertTrue(facade.getGroupsForSubjectId("casuser").isEmpty());
    }
}


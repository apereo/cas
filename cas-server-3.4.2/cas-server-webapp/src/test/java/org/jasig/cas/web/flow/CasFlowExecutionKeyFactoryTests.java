package org.jasig.cas.web.flow;

import org.junit.Test;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.execution.repository.snapshot.FlowExecutionSnapshotFactory;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: battags
 * Date: 3/13/11
 * Time: 11:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class CasFlowExecutionKeyFactoryTests {

    @Test
    public void testEncryptionDecryption() throws Exception {
        final CasFlowExecutionKeyFactory factory = new CasFlowExecutionKeyFactory(mock(ConversationManager.class), mock(FlowExecutionSnapshotFactory.class));
        final String originalString = "5f43a855f34852343434234343Ze1s1";
        final String encryptedVersion = factory.encrypt(originalString);
        final String decryptedVersion = factory.decrypt(encryptedVersion);

        assertEquals(originalString, decryptedVersion);
    }
}

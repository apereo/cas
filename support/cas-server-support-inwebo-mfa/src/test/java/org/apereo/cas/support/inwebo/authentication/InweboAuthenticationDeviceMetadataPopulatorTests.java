package org.apereo.cas.support.inwebo.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InweboAuthenticationDeviceMetadataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFAProvider")
public class InweboAuthenticationDeviceMetadataPopulatorTests {
    private final InweboAuthenticationDeviceMetadataPopulator populator =
        new InweboAuthenticationDeviceMetadataPopulator();

    @Test
    public void verifyPopulator() {
        val credentials = new InweboCredential();
        credentials.setDeviceName("MyDeviceName");
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        assertTrue(this.populator.supports(credentials));
        assertNotNull(populator.toString());
        this.populator.populateAttributes(builder, new DefaultAuthenticationTransactionFactory().newTransaction(credentials));
        val auth = builder.build();
        assertEquals(
            credentials.getDeviceName(),
            auth.getAttributes()
                .get(InweboAuthenticationDeviceMetadataPopulator.ATTRIBUTE_NAME_INWEBO_AUTHENTICATION_DEVICE).get(0));
    }
}

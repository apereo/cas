package org.apereo.cas.support.inwebo.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

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
class InweboAuthenticationDeviceMetadataPopulatorTests {
    private final InweboAuthenticationDeviceMetadataPopulator populator =
        new InweboAuthenticationDeviceMetadataPopulator();

    @Test
    void verifyPopulator() {
        val credentials = new InweboCredential();
        credentials.setDeviceName("MyDeviceName");
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        assertTrue(this.populator.supports(credentials));
        assertNotNull(populator.toString());
        this.populator.populateAttributes(builder, CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials));
        val auth = builder.build();
        assertEquals(
            credentials.getDeviceName(),
            auth.getAttributes()
                .get(InweboAuthenticationDeviceMetadataPopulator.ATTRIBUTE_NAME_INWEBO_AUTHENTICATION_DEVICE).getFirst());
    }
}

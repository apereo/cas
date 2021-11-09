package org.apereo.cas.authentication;

import org.apereo.cas.authentication.metadata.ClientInfoAuthenticationMetaDataPopulator;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ClientInfoAuthenticationMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("AuthenticationMetadata")
public class ClientInfoAuthenticationMetaDataPopulatorTests {

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.000");
        request.setLocalAddr("123.456.789.000");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        val populator = new ClientInfoAuthenticationMetaDataPopulator();
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val builder = DefaultAuthenticationBuilder.newInstance(CoreAuthenticationTestUtils.getAuthentication());
        assertTrue(populator.supports(c));
        populator.populateAttributes(builder, new DefaultAuthenticationTransactionFactory().newTransaction(c));
        val authn = builder.build();
        val attributes = authn.getAttributes();
        assertTrue(attributes.containsKey(ClientInfoAuthenticationMetaDataPopulator.ATTRIBUTE_CLIENT_IP_ADDRESS));
        assertTrue(attributes.containsKey(ClientInfoAuthenticationMetaDataPopulator.ATTRIBUTE_SERVER_IP_ADDRESS));
        assertTrue(attributes.containsKey(ClientInfoAuthenticationMetaDataPopulator.ATTRIBUTE_USER_AGENT));
        assertTrue(attributes.containsKey(ClientInfoAuthenticationMetaDataPopulator.ATTRIBUTE_GEO_LOCATION));
    }
}

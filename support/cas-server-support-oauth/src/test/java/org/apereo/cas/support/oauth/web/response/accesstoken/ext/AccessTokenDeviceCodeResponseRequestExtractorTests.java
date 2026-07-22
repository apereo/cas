package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccessTokenDeviceCodeResponseRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OAuth")
class AccessTokenDeviceCodeResponseRequestExtractorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("accessTokenDeviceCodeResponseRequestExtractor")
    private AccessTokenGrantRequestExtractor accessTokenDeviceCodeResponseRequestExtractor;

    @Test
    void verifyExtraction() throws Throwable {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        service.setScopes(CollectionUtils.wrapSet("read", "profile"));
        servicesManager.save(service);

        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.DEVICE_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.DEVICE_CODE, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.SCOPE, "read");
        val context = new JEEContext(request, response);
        assertTrue(accessTokenDeviceCodeResponseRequestExtractor.supports(context));
        val result = accessTokenDeviceCodeResponseRequestExtractor.extract(context);
        assertNotNull(result);
        assertEquals(1, result.getScopes().size());
    }
}


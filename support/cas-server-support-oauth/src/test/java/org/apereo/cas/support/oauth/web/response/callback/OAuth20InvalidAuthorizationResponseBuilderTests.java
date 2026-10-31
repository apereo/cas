package org.apereo.cas.support.oauth.web.response.callback;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.view.RedirectView;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20InvalidAuthorizationResponseBuilderTests}.
 *
 * @author Julien Huon
 * @since 6.4.0
 */
@Tag("OAuth")
class OAuth20InvalidAuthorizationResponseBuilderTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauthInvalidAuthorizationBuilder")
    private OAuth20InvalidAuthorizationResponseBuilder oauthInvalidAuthorizationBuilder;

    @Test
    void verifyRequestWithoutCallback() throws Throwable {
        val request = new MockHttpServletRequest();

        assertFalse(oauthInvalidAuthorizationBuilder.supports(request));

        request.setAttribute(OAuth20Constants.ERROR, OAuth20Constants.INVALID_REQUEST);
        assertTrue(oauthInvalidAuthorizationBuilder.supports(request));
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR,
            oauthInvalidAuthorizationBuilder.build(request).getViewName());

        request.setAttribute(OAuth20Constants.ERROR_WITH_CALLBACK, false);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR,
            oauthInvalidAuthorizationBuilder.build(request).getViewName());
    }

    @Test
    void verifyRequestWithCallbackAndDescription() throws Throwable {
        val service = addRegisteredService();
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://github.com/apereo/cas");

        assertFalse(oauthInvalidAuthorizationBuilder.supports(request));

        request.setAttribute(OAuth20Constants.ERROR, OAuth20Constants.INVALID_REQUEST);
        request.setAttribute(OAuth20Constants.ERROR_DESCRIPTION, "Invalid Request Description");
        request.setAttribute(OAuth20Constants.ERROR_WITH_CALLBACK, true);
        assertTrue(oauthInvalidAuthorizationBuilder.supports(request));

        val mv = oauthInvalidAuthorizationBuilder.build(request);
        assertInstanceOf(RedirectView.class, mv.getView());
        val mvView = (RedirectView) mv.getView();
        assertEquals("https://github.com/apereo/cas", mvView.getUrl());
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ERROR));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ERROR_DESCRIPTION));
        assertFalse(mv.getModel().containsKey(OAuth20Constants.STATE));

        val error = mv.getModel().get(OAuth20Constants.ERROR).toString();
        assertEquals(OAuth20Constants.INVALID_REQUEST, error);
        val errorDescription = mv.getModel().get(OAuth20Constants.ERROR_DESCRIPTION).toString();
        assertEquals("Invalid Request Description", errorDescription);
    }

    @Test
    void verifyRequestWithCallbackWithoutDescription() throws Throwable {
        val service = addRegisteredService();
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://github.com/apereo/cas");
        request.addParameter(OAuth20Constants.STATE, "abcdefgh");
        request.addParameter(OAuth20Constants.RESPONSE_MODE, OAuth20ResponseModeTypes.FORM_POST.getType());
        request.addParameter("ParameterWhichShouldNotComingBack", "notAtAll");

        assertFalse(oauthInvalidAuthorizationBuilder.supports(request));

        request.setAttribute(OAuth20Constants.ERROR, OAuth20Constants.INVALID_REQUEST);
        request.setAttribute(OAuth20Constants.ERROR_WITH_CALLBACK, true);
        assertTrue(oauthInvalidAuthorizationBuilder.supports(request));

        val mv = oauthInvalidAuthorizationBuilder.build(request);
        assertEquals(CasWebflowConstants.VIEW_ID_POST_RESPONSE, mv.getViewName());
        assertTrue(mv.getModel().containsKey("originalUrl"));
        assertTrue(mv.getModel().containsKey("parameters"));

        val originalUrl = mv.getModel().get("originalUrl");
        assertEquals("https://github.com/apereo/cas", originalUrl);
        val parameters = (Map) mv.getModel().get("parameters");

        assertTrue(parameters.containsKey(OAuth20Constants.ERROR));
        assertFalse(parameters.containsKey(OAuth20Constants.ERROR_DESCRIPTION));
        assertTrue(parameters.containsKey(OAuth20Constants.STATE));
        assertEquals(OAuth20Constants.INVALID_REQUEST, parameters.get(OAuth20Constants.ERROR));
        assertEquals("abcdefgh", parameters.get(OAuth20Constants.STATE));
    }
}

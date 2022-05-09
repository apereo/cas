package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is {@link OAuth20InvalidAuthorizationResponseBuilderTests}.
 *
 * @author Julien Huon
 * @since 6.4.0
 */
@Tag("OAuth")
public class OAuth20InvalidAuthorizationResponseBuilderTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauthInvalidAuthorizationBuilder")
    private OAuth20InvalidAuthorizationResponseBuilder oauthInvalidAuthorizationBuilder;

    @Test
    public void verifyRequestWithoutCallback() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val context = new JEEContext(request, response);
        assertFalse(oauthInvalidAuthorizationBuilder.supports(context));

        context.setRequestAttribute(OAuth20Constants.ERROR, OAuth20Constants.INVALID_REQUEST);
        assertTrue(oauthInvalidAuthorizationBuilder.supports(context));
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR,
            oauthInvalidAuthorizationBuilder.build(context).getViewName());

        context.setRequestAttribute(OAuth20Constants.ERROR_WITH_CALLBACK, false);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR,
            oauthInvalidAuthorizationBuilder.build(context).getViewName());
    }

    @Test
    public void verifyRequestWithCallbackAndDescription() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://github.com/apereo/cas");

        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        assertFalse(oauthInvalidAuthorizationBuilder.supports(context));

        context.setRequestAttribute(OAuth20Constants.ERROR, OAuth20Constants.INVALID_REQUEST);
        context.setRequestAttribute(OAuth20Constants.ERROR_DESCRIPTION, "Invalid Request Description");
        context.setRequestAttribute(OAuth20Constants.ERROR_WITH_CALLBACK, true);
        assertTrue(oauthInvalidAuthorizationBuilder.supports(context));

        val mv = oauthInvalidAuthorizationBuilder.build(context);
        assertTrue(mv.getView() instanceof RedirectView);
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
    public void verifyRequestWithCallbackWithoutDescription() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://github.com/apereo/cas");
        request.addParameter(OAuth20Constants.STATE, "abcdefgh");
        request.addParameter(OAuth20Constants.RESPONSE_MODE, OAuth20ResponseModeTypes.FORM_POST.getType());
        request.addParameter("ParameterWhichShouldNotComingBack", "notAtAll");

        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        assertFalse(oauthInvalidAuthorizationBuilder.supports(context));

        context.setRequestAttribute(OAuth20Constants.ERROR, OAuth20Constants.INVALID_REQUEST);
        context.setRequestAttribute(OAuth20Constants.ERROR_WITH_CALLBACK, true);
        assertTrue(oauthInvalidAuthorizationBuilder.supports(context));

        val mv = oauthInvalidAuthorizationBuilder.build(context);
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

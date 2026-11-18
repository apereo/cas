package org.apereo.cas.support.oauth.web.response.callback;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriUtils;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOAuth20AuthorizationModelAndViewBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OAuth")
class DefaultOAuth20AuthorizationModelAndViewBuilderTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauthAuthorizationModelAndViewBuilder")
    private OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder;

    @Test
    void verifyFragment() throws Throwable {
        val registeredService = getRegisteredService("example", CLIENT_SECRET, new LinkedHashSet<>());
        servicesManager.save(registeredService);
        val mv = oauthAuthorizationModelAndViewBuilder.build(registeredService, OAuth20ResponseModeTypes.FRAGMENT,
            "https://github.com/apereo/cas?one=value&two=value&code=123456",
            Map.of("code", "123456"));
        assertInstanceOf(RedirectView.class, mv.getView());
        val view = (RedirectView) mv.getView();
        assertTrue(mv.getModel().isEmpty());
        assertEquals("code=123456", new URI(view.getUrl()).getFragment());

    }

    @Test
    void verifyFragmentEncodesParameters() throws Throwable {
        val registeredService = getRegisteredService("example", CLIENT_SECRET, new LinkedHashSet<>());
        servicesManager.save(registeredService);
        val parameters = new LinkedHashMap<String, String>();
        parameters.put("code", "OC-123456");
        parameters.put("state", "{\"b5_state\":\"5tIE60Dfgh0fOU9hm01Ocw\",\"redirect_uuid\":\"5khyxbec4l4c7vmzl2lrowd4k4\"}");
        parameters.put("nonce", "a&b=c#d");
        parameters.put("error_description", "User denied access");
        val mv = oauthAuthorizationModelAndViewBuilder.build(registeredService, OAuth20ResponseModeTypes.FRAGMENT,
            "https://github.com/apereo/cas?one=%7Bvalue%7D", parameters);
        val response = new MockHttpServletResponse();
        Objects.requireNonNull(mv.getView()).render(mv.getModel(), new MockHttpServletRequest(), response);
        val location = new URI(Objects.requireNonNull(response.getRedirectedUrl()));
        assertEquals("one=%7Bvalue%7D", location.getRawQuery());
        val fragment = Arrays.stream(location.getRawFragment().split("&"))
            .map(pair -> pair.split("=", 2))
            .collect(Collectors.toMap(pair -> UriUtils.decode(pair[0], StandardCharsets.UTF_8),
                pair -> UriUtils.decode(pair[1], StandardCharsets.UTF_8), (first, second) -> second, LinkedHashMap::new));
        assertEquals(parameters, fragment);
    }
}

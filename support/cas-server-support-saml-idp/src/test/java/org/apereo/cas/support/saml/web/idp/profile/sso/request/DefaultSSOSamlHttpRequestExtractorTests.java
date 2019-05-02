package org.apereo.cas.support.saml.web.idp.profile.sso.request;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.web.idp.profile.sso.UrlDecodingHTTPRedirectDeflateDecoder;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultSSOSamlHttpRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("SAML")
public class DefaultSSOSamlHttpRequestExtractorTests extends BaseSamlIdPConfigurationTests {
    private static final String SAML_REQUEST = "pZNBj9owEIX%2FSm4%2BJcYQFrAIUgSqhLRtEWx72MvK6wys1cROPeNu%2Bu%2FrBGg57O6lp0j"
        + "j5%2FnevHGWqJq6lWWgF7uHnwGQkhIRPBln185iaMAfwP8yGr7t7wv2QtSi5BxBBw"
        + "%2BZVh4q12XaNTLPJ9z509PJu9ByMZmMeYidkPcMji3X534sKYm8eQ4EZ4KxpwtiayvoCjZlySY6MVb1Nv5BTRtxlrJGEYYfYpwF5OtY2rjucPjKEV2msO1Yst0U7Kla5CKfwiSdPefjNJ"
        + "%2Brebq4Ox7TyUwvcq0Wo3keQVvEELlIylLBxiMxS0fTVIgHcSenuRyJR5Z8j1MMRsbZiCVdU1uU%2FVAFC95Kp9CgtKoBlKTlofx8L6NQqmuOt1faj%2B%2B03pHTrmarZa%2BWgzu%2F%2Bp"
        + "%2FUGyBVKVJLfttxeV78l%2Bhgu9m52ujfSVnX7nXtQREUjHyIm%2FrkfAz7fc8iE0PFVOlxkMpgsQVtjgYqxq%2BYy9OCanhocesEHSVr18SFGuyThU5puk59q1rXMcc9HFcfRq2l7nWxvI"
        + "ufV%2BerXUwSdEQ%2BeBUtOU%2BXAN5sfj57x%2Bjf09vfZPUH";

    @Test
    public void verifyActionWithExplicitUrlDecoding() {
        val ext = new DefaultSSOSamlHttpRequestExtractor(this.openSamlConfigBean.getParserPool());
        val decoded = EncodingUtils.urlDecode(SAML_REQUEST);
        val request = getMockHttpServletRequest(decoded);
        val decoder = new UrlDecodingHTTPRedirectDeflateDecoder(false);
        val result = ext.extract(request, decoder, AuthnRequest.class);
        assertNotNull(result.getKey());
        assertNotNull(result.getValue());
    }


    @Test
    public void verifyActionWithoutExplicitUrlDecoding() {
        val ext = new DefaultSSOSamlHttpRequestExtractor(this.openSamlConfigBean.getParserPool());
        val request = getMockHttpServletRequest(SAML_REQUEST);
        val decoder = new UrlDecodingHTTPRedirectDeflateDecoder(true);
        val result = ext.extract(request, decoder, AuthnRequest.class);
        assertNotNull(result.getKey());
        assertNotNull(result.getValue());
    }

    private static MockHttpServletRequest getMockHttpServletRequest(final String decoded) {
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.GET.name());
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, decoded);
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, "RelayStateData");
        return request;
    }
}

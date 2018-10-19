package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.logout.LogoutHttpMessage;
import org.apereo.cas.util.EncodingUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link SamlLogoutHttpMessage}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class SamlLogoutHttpMessage extends LogoutHttpMessage {
    private static final long serialVersionUID = 2211426358390460475L;

    public SamlLogoutHttpMessage(final URL url, final String message, final boolean asynchronous) {
        super(url, message, asynchronous);
    }

    @Override
    protected String formatOutputMessageInternal(final String message) {
        return "SAMLRequest=" + EncodingUtils.encodeBase64(message.getBytes(StandardCharsets.UTF_8), false);
    }
}

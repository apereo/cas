package org.apereo.cas.support.saml.web.idp.profile.slo;

import module java.base;
import org.apereo.cas.logout.LogoutHttpMessage;
import org.springframework.http.MediaType;

/**
 * A logout message sent with the SOAP binding. Its body is the SOAP envelope itself, sent as {@code text/xml}, so it is
 * not URL-encoded or wrapped in a form parameter like the other logout messages.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class SamlIdPSoapLogoutHttpMessage extends LogoutHttpMessage {
    @Serial
    private static final long serialVersionUID = -2706155285914553148L;

    public SamlIdPSoapLogoutHttpMessage(final URL url, final String envelope, final boolean asynchronous) {
        super(url, envelope, asynchronous);
        setContentType(MediaType.TEXT_XML_VALUE);
    }

    @Override
    protected String formatOutputMessageInternal(final String message) {
        return message;
    }
}

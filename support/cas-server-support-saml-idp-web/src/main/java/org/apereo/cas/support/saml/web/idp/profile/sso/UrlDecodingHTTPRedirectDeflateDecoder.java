package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.util.EncodingUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPRedirectDeflateDecoder;

import java.io.InputStream;

/**
 * This is {@link UrlDecodingHTTPRedirectDeflateDecoder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class UrlDecodingHTTPRedirectDeflateDecoder extends HTTPRedirectDeflateDecoder {
    private final boolean urlDecodeMessage;

    @Override
    protected InputStream decodeMessage(final String message) throws MessageDecodingException {
        val decoded = this.urlDecodeMessage ? EncodingUtils.urlDecode(message) : message;
        return super.decodeMessage(decoded);
    }
}

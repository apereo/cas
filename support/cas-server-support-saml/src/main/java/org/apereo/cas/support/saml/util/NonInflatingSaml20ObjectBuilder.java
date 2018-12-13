package org.apereo.cas.support.saml.util;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.EncodingUtils;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link NonInflatingSaml20ObjectBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.3.7
 */
public class NonInflatingSaml20ObjectBuilder extends AbstractSaml20ObjectBuilder {
    public NonInflatingSaml20ObjectBuilder(final OpenSamlConfigBean configBean) {
        super(configBean);
    }

    @Override
    public String decodeSamlAuthnRequest(final String encodedRequestXmlString) {
        if (StringUtils.isEmpty(encodedRequestXmlString)) {
            return null;
        }
        final byte[] decodedBytes = EncodingUtils.decodeBase64(encodedRequestXmlString);
        if (decodedBytes == null) {
            return null;
        }
        final String inflated = CompressionUtils.decodeByteArrayToString(decodedBytes);
        if (!StringUtils.isEmpty(inflated)) {
            return inflated;
        }
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}

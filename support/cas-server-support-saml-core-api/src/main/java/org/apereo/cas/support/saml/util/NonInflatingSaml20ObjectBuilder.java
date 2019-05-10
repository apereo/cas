package org.apereo.cas.support.saml.util;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.util.CompressionUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link NonInflatingSaml20ObjectBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.3.7
 */
public class NonInflatingSaml20ObjectBuilder extends AbstractSaml20ObjectBuilder {
    private static final long serialVersionUID = 4737200174453971436L;

    public NonInflatingSaml20ObjectBuilder(final OpenSamlConfigBean configBean) {
        super(configBean);
    }

    @Override
    protected String inflateAuthnRequest(final byte[] decodedBytes) {
        val inflated = CompressionUtils.decodeByteArrayToString(decodedBytes);
        if (!StringUtils.isEmpty(inflated)) {
            return inflated;
        }
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}

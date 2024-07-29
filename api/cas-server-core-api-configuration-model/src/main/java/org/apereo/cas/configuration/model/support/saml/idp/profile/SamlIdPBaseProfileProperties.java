package org.apereo.cas.configuration.model.support.saml.idp.profile;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link SamlIdPBaseProfileProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)

public class SamlIdPBaseProfileProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -8100516679034234656L;

    /**
     * Whether the initial request should be explicitly url-decoded
     * before it's consumed by the decoder.
     */
    private boolean urlDecodeRedirectRequest;
}

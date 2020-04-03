package org.apereo.cas.configuration.model.support.saml.googleapps;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link GoogleAppsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated Since 6.2
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-saml-googleapps")
@Accessors(chain = true)
@Deprecated(since = "6.2.0")
public class GoogleAppsProperties implements Serializable {

    private static final long serialVersionUID = -5133482766495375325L;

    /**
     * The public key location that is also shared with google apps.
     */
    private String publicKeyLocation = "file:/etc/cas/public.key";

    /**
     * The private key location that is used to sign responses, etc.
     */
    private String privateKeyLocation = "file:/etc/cas/private.key";

    /**
     * Signature algorithm used to generate keys.
     */
    private String keyAlgorithm = "RSA";
}

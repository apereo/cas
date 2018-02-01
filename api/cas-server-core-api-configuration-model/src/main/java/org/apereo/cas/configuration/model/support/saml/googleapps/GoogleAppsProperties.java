package org.apereo.cas.configuration.model.support.saml.googleapps;

import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link GoogleAppsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
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

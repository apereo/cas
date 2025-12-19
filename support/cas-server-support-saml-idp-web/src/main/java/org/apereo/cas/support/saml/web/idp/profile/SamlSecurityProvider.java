package org.apereo.cas.support.saml.web.idp.profile;

import module java.base;

/**
 * This is {@link SamlSecurityProvider}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SamlSecurityProvider extends Provider {
    @Serial
    private static final long serialVersionUID = 3941609616158174459L;

    public SamlSecurityProvider() {
        super("CAS-SAML2", "1.0.0", "Apereo CAS SAML2 IdP Security Provider");
        put("Alg.Alias.Signature.ECDSAwithSHA1", "ECDSA");
        put("Alg.Alias.Signature.ECDSAwithSHA256", "ECDSA");
        put("Alg.Alias.Signature.ECDSAwithSHA384", "ECDSA");
        put("Alg.Alias.Signature.ECDSAwithSHA512", "ECDSA");
    }
}

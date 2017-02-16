package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import java.util.List;

/**
 * This is {@link IdPSamlObjectSigner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class IdPSamlObjectSigner extends BaseSamlObjectSigner {
    public IdPSamlObjectSigner(final List overrideSignatureReferenceDigestMethods, final List overrideSignatureAlgorithms,
                               final List overrideBlackListedSignatureAlgorithms, final List overrideWhiteListedAlgorithms) {
        super(overrideSignatureReferenceDigestMethods, overrideSignatureAlgorithms, overrideBlackListedSignatureAlgorithms,
                overrideWhiteListedAlgorithms);
    }
}

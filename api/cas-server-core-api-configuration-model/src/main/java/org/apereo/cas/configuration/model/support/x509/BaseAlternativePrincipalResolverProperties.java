package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link BaseAlternativePrincipalResolverProperties}.
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-x509-webflow")
@Getter
@Setter
@Accessors(chain = true)
public abstract class BaseAlternativePrincipalResolverProperties implements Serializable {
    private static final long serialVersionUID = 4770829035414038072L;
    /**
     * Attribute name that will be used by X509 principal resolvers if the main attribute in the
     * certificate is not present. This only applies to principal resolvers that are looking
     * for attributes in the certificate that are not common to all certificates.
     * (e.g. SUBJECT_ALT_NAME, CN_EDIPI)
     * <p>
     * This assumes you would rather get something like the subjectDn rather than null
     * where null would allow falling through to another authentication mechanism.
     * <p>
     * Currently supported values are: subjectDn, sigAlgOid, subjectX500Principal.
     */
    private String alternatePrincipalAttribute;

}

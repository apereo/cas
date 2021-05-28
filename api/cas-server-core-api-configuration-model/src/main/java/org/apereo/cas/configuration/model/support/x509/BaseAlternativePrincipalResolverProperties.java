package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("BaseAlternativePrincipalResolverProperties")
public abstract class BaseAlternativePrincipalResolverProperties implements Serializable {
    private static final long serialVersionUID = 4770829035414038072L;

    /**
     * Attribute name that will be used by X509 principal resolvers if the main attribute in the
     * certificate is not present. This only applies to principal resolvers that are looking
     * for attributes in the certificate that are not common to all certificates.
     * (e.g. {@code SUBJECT_ALT_NAME}, {@code CN_EDIPI})
     * <p>
     * This assumes you would rather get something like the {@code subjectDn} rather than {@code null}
     * where {@code null} would allow falling through to another authentication mechanism.
     * <p>
     * Currently supported values are: {@code subjectDn}, {@code sigAlgOid}, {@code subjectX500Principal}.
     */
    private String alternatePrincipalAttribute;

}

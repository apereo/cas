package org.apereo.cas.adaptors.x509.util;

import org.apereo.cas.configuration.model.support.x509.SubjectDnPrincipalResolverProperties;

import lombok.experimental.UtilityClass;

import javax.security.auth.x500.X500Principal;

/**
 * This is {@link X509AuthenticationUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@UtilityClass
public class X509AuthenticationUtils {
    /**
     * Gets subject dn format.
     *
     * @param format the format
     * @return the subject dn format
     */
    public static String getSubjectDnFormat(final SubjectDnPrincipalResolverProperties.SubjectDnFormat format) {
        switch (format) {
            case RFC1779:
                return X500Principal.RFC1779;
            case RFC2253:
                return X500Principal.RFC2253;
            case CANONICAL:
                return X500Principal.CANONICAL;
            case DEFAULT:
            default:
                return null;
        }
    }
}

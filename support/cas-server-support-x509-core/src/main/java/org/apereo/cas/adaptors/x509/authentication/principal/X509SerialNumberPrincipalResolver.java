package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.PrincipalFactory;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * Returns a new principal based on the serial number of the certificate.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@ToString(callSuper = true)
@RequiredArgsConstructor
public class X509SerialNumberPrincipalResolver extends AbstractX509PrincipalResolver {

    private static final int DEFAULT_RADIX = 10;

    private final int radix;

    private final boolean zeroPadding;

    public X509SerialNumberPrincipalResolver() {
        this(DEFAULT_RADIX, false);
    }

    public X509SerialNumberPrincipalResolver(final IPersonAttributeDao attributeRepository,
                                             final PrincipalFactory principalFactory, final boolean returnNullIfNoAttributes,
                                             final String principalAttributeName,
                                             final boolean useCurrentPrincipalId,
                                             final boolean resolveAttributes,
                                             final Set<String> activeAttributeRepositoryIdentifiers) {
        this(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, DEFAULT_RADIX, false,
            useCurrentPrincipalId, resolveAttributes, activeAttributeRepositoryIdentifiers);
    }

    public X509SerialNumberPrincipalResolver(final IPersonAttributeDao attributeRepository,
                                             final PrincipalFactory principalFactory, final boolean returnNullIfNoAttributes,
                                             final String principalAttributeName, final int radix, final boolean zeroPadding,
                                             final boolean useCurrentPrincipalId,
                                             final boolean resolveAttributes,
                                             final Set<String> activeAttributeRepositoryIdentifiers) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, useCurrentPrincipalId, resolveAttributes,
            activeAttributeRepositoryIdentifiers);
        this.radix = radix;
        this.zeroPadding = zeroPadding;
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        val principal = certificate.getSerialNumber().toString(radix);
        if (zeroPadding && principal.length() % 2 != 0) {
            return '0' + principal;
        }
        return principal;
    }
}

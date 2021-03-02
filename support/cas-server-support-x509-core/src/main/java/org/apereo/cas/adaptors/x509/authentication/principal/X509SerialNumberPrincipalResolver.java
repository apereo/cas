package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;

import lombok.ToString;
import lombok.val;

import java.security.cert.X509Certificate;

/**
 * Returns a new principal based on the serial number of the certificate.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@ToString(callSuper = true)
public class X509SerialNumberPrincipalResolver extends AbstractX509PrincipalResolver {

    private static final int DEFAULT_RADIX = 10;

    private final int radix;

    private final boolean zeroPadding;

    public X509SerialNumberPrincipalResolver(final PrincipalResolutionContext context) {
        this(context, DEFAULT_RADIX, false);
    }

    public X509SerialNumberPrincipalResolver(final PrincipalResolutionContext context,
                                             final int radix, final boolean zeroPadding) {
        super(context);
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

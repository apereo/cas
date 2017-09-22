package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.security.cert.X509Certificate;

/**
 * Returns a new principal based on the Sereial Number of the certificate.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class X509SerialNumberPrincipalResolver extends AbstractX509PrincipalResolver {

    /**
     * Radix to use in {@code toString} method.
     */
    private int radix = 10;

    private boolean zeroPadding;

    public X509SerialNumberPrincipalResolver() {
    }

    public X509SerialNumberPrincipalResolver(final int radix, final boolean zeroPadding) {
        this.radix = radix;
        this.zeroPadding = zeroPadding;
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        final String principal = certificate.getSerialNumber().toString(radix);
        if (zeroPadding && principal.length() % 2 == 1) {
            return "0" + principal;
        }
        return principal;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}

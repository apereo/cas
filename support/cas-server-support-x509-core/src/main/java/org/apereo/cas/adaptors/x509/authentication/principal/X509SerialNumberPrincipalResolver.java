package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;

import lombok.Setter;
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
@Setter
public class X509SerialNumberPrincipalResolver extends AbstractX509PrincipalResolver {

    private static final int DEFAULT_RADIX = 10;

    private int radix = DEFAULT_RADIX;

    private boolean zeroPadding;

    public X509SerialNumberPrincipalResolver(final PrincipalResolutionContext context) {
        super(context);
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

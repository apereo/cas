package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;

import lombok.Setter;
import lombok.ToString;

import java.security.cert.X509Certificate;

/**
 * This class is targeted at usage for mapping to an existing user record. It
 * can construct a highly-likely unique DN based on a certificate's serialnumber
 * and its issuerDN. example:
 * SERIALNUMBER=20267647332258882251479793556682961758, SERIALNUMBER=200301,
 * CN=Citizen CA, C=BE see RFC3280 The combination of a certificate serial
 * number and the issuerDN *should* be unique: - The certificate serialNumber is
 * by its nature unique for a certain issuer. - The issuerDN is RECOMMENDED to
 * be unique. Both the serial number and the issuerDN are REQUIRED in a
 * certificate. Note: comparison rules state the compare should be
 * case-insensitive. LDAP value description: EQUALITY distinguishedNameMatch
 * SYNTAX 1.3.6.1.4.1.1466.115.121.1.12 [=distinguishedName]
 *
 * @author Jan Van der Velpen
 * @since 3.1
 */
@ToString(callSuper = true)
@Setter
public class X509SerialNumberAndIssuerDNPrincipalResolver extends AbstractX509PrincipalResolver {

    /**
     * Prefix for Certificate Serial Number.
     */
    private String serialNumberPrefix = "SERIALNUMBER=";

    /**
     * Prefix for Value Delimiter.
     */
    private String valueDelimiter = ", ";

    public X509SerialNumberAndIssuerDNPrincipalResolver(final PrincipalResolutionContext context) {
        super(context);
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        return new StringBuilder(this.serialNumberPrefix)
            .append(certificate.getSerialNumber()).append(this.valueDelimiter)
            .append(certificate.getIssuerDN().getName()).toString();
    }
}

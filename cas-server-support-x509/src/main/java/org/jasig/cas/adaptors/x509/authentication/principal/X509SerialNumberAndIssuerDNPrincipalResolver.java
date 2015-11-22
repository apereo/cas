package org.jasig.cas.adaptors.x509.authentication.principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
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
@Component("x509SerialNumberAndIssuerDNPrincipalResolver")
public final class X509SerialNumberAndIssuerDNPrincipalResolver extends AbstractX509PrincipalResolver {

    /** Prefix for Certificate Serial Number. */
    @NotNull
    private String serialNumberPrefix = "SERIALNUMBER=";

    /** Prefix for Value Delimiter. */
    @NotNull
    private String valueDelimiter = ", ";

    /**
     * Sets a prefix for the certificate serialnumber (default: "SERIALNUMBER=").
     *
     * @param serialNumberPrefix The serialNumberPrefix to set.
     */
    @Autowired
    public void setSerialNumberPrefix(@Value("${cas.x509.authn.principal.serial.no.prefix:SERIALNUMBER=}")
                                          final String serialNumberPrefix) {
        this.serialNumberPrefix = serialNumberPrefix;
    }

    /**
     * Sets a delimiter to separate the two certificate properties in the string.
     * (default: ", ")
     *
     * @param valueDelimiter The valueDelimiter to set.
     */
    @Autowired
    public void setValueDelimiter(@Value("${cas.x509.authn.principal.value.delim:, }")
                                      final String valueDelimiter) {
        this.valueDelimiter = valueDelimiter;
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        final StringBuilder builder = new StringBuilder(this.serialNumberPrefix);
        builder.append(certificate.getSerialNumber().toString());
        builder.append(this.valueDelimiter);
        builder.append(certificate.getIssuerDN().getName());
        return builder.toString();
    }
}

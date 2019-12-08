package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.StubPersonAttributeDao;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Set;

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
public class X509SerialNumberAndIssuerDNPrincipalResolver extends AbstractX509PrincipalResolver {

    /**
     * Prefix for Certificate Serial Number.
     */
    private final String serialNumberPrefix;

    /**
     * Prefix for Value Delimiter.
     */
    private final String valueDelimiter;

    public X509SerialNumberAndIssuerDNPrincipalResolver(final String serialNumberPrefix, final String valueDelimiter) {
        this(new StubPersonAttributeDao(new HashMap<>(0)), PrincipalFactoryUtils.newPrincipalFactory(), false,
            null, serialNumberPrefix, valueDelimiter, false, true,
            CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD));
    }

    public X509SerialNumberAndIssuerDNPrincipalResolver(final IPersonAttributeDao attributeRepository,
                                                        final PrincipalFactory principalFactory,
                                                        final boolean returnNullIfNoAttributes,
                                                        final String principalAttributeName,
                                                        final String serialNumberPrefix,
                                                        final String valueDelimiter,
                                                        final boolean useCurrentPrincipalId,
                                                        final boolean resolveAttributes,
                                                        final Set<String> activeAttributeRepositoryIdentifiers) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, useCurrentPrincipalId, resolveAttributes,
            activeAttributeRepositoryIdentifiers);
        this.serialNumberPrefix = StringUtils.defaultString(serialNumberPrefix, "SERIALNUMBER=");
        this.valueDelimiter = StringUtils.defaultIfBlank(valueDelimiter, ", ");
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        return new StringBuilder(this.serialNumberPrefix)
            .append(certificate.getSerialNumber()).append(this.valueDelimiter)
            .append(certificate.getIssuerDN().getName()).toString();
    }
}

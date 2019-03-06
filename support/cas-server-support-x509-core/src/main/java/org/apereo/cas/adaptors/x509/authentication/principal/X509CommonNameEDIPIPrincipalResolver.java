package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.PrincipalFactory;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This is {@link X509CommonNameEDIPIPrincipalResolver}.
 * This resolver locates and returns the Electronic Data Interchange Personal Identifier (EDIPI) from
 * the Common Name(CN). The EDIPI is a unique number assigned to a record in the Defense Enrollment
 * and Eligibility Reporting System (DEERS) database.The Common Access Card (CAC), which is issued by
 * the Department of Defense through DEERS, has an EDIPI on the card.  The EDIPI is a 10-digit integer
 * placed at the end of the CN.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Slf4j
@ToString(callSuper = true)
@NoArgsConstructor
public class X509CommonNameEDIPIPrincipalResolver extends AbstractX509PrincipalResolver {

    private static final String COMMON_NAME_VAR = "CN";

    private static final int EDIPI_LENGTH = 10;

    public X509CommonNameEDIPIPrincipalResolver(final IPersonAttributeDao attributeRepository,
                                                final PrincipalFactory principalFactory,
                                                final boolean returnNullIfNoAttributes,
                                                final String principalAttributeName,
                                                final String alternatePrincipalAttribute,
                                                final boolean useCurrentPrincipalId,
                                                final boolean resolveAttributes,
                                                final Set<String> activeAttributeRepositoryIdentifiers) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, alternatePrincipalAttribute, useCurrentPrincipalId,
            resolveAttributes, activeAttributeRepositoryIdentifiers);
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        val subjectDn = certificate.getSubjectDN().getName();
        LOGGER.debug("Creating principal based on subject DN [{}]", subjectDn);
        if (StringUtils.isBlank(subjectDn)) {
            return getAlternatePrincipal(certificate);
        }
        val commonName = retrieveTheCommonName(subjectDn);
        if (StringUtils.isBlank(commonName)) {
            return getAlternatePrincipal(certificate);
        }
        val result = retrieveTheEDIPI(commonName);
        if (StringUtils.isBlank(result)) {
            return getAlternatePrincipal(certificate);
        }
        LOGGER.debug("Final principal id extracted from [{}] is [{}]", subjectDn, result);
        return result;
    }

    private static String retrieveTheCommonName(final String inSubjectDN) {
        var commonNameFound = false;
        var tempCommonName = StringUtils.EMPTY;
        val st = new StringTokenizer(inSubjectDN, ",");
        while (!commonNameFound && st.hasMoreTokens()) {
            val token = st.nextToken();
            if (isTokenCommonName(token)) {
                commonNameFound = true;
                tempCommonName = token;
            }
        }
        return StringUtils.remove(tempCommonName, COMMON_NAME_VAR + '=');
    }

    private static String retrieveTheEDIPI(final String commonName) {
        var found = false;
        var tempEDIPI = StringUtils.EMPTY;
        val st = new StringTokenizer(commonName, ".");
        while (!found && st.hasMoreTokens()) {
            val token = st.nextToken();
            if (isTokenEDIPI(token)) {
                found = true;
                tempEDIPI = token;
            }
        }
        return tempEDIPI;
    }

    /**
     * This method determines whether or not the input token is the Common Name (CN).
     *
     * @param inToken The input token to be tested
     * @return Returns boolean value indicating whether or not the token string is the Common Name (CN) number
     */
    private static boolean isTokenCommonName(final String inToken) {
        val st = new StringTokenizer(inToken, "=");
        return st.nextToken().equals(COMMON_NAME_VAR);
    }

    private static boolean isTokenEDIPI(final String inToken) {
        return inToken.length() == EDIPI_LENGTH && NumberUtils.isCreatable(inToken);
    }
}

package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
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
public class X509CommonNameEDIPIPrincipalResolver extends AbstractX509PrincipalResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(X509CommonNameEDIPIPrincipalResolver.class);
    private static final String COMMON_NAME_VAR = "CN";
    private static final int EDIPI_LENGTH = 10;

    public X509CommonNameEDIPIPrincipalResolver() {
    }

    public X509CommonNameEDIPIPrincipalResolver(final IPersonAttributeDao attributeRepository,
                                                final PrincipalFactory principalFactory,
                                                final boolean returnNullIfNoAttributes,
                                                final String principalAttributeName) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        final String subjectDn = certificate.getSubjectDN().getName();
        LOGGER.debug("Creating principal based on subject DN [{}]", subjectDn);
        if (StringUtils.isBlank(subjectDn)) {
            return null;
        }

        final String commonName = retrieveTheCommonName(subjectDn);
        if (StringUtils.isBlank(commonName)) {
            return null;
        }
        final String result = retrieveTheEDIPI(commonName);
        LOGGER.debug("Final principal id extracted from [{}] is [{}]", subjectDn, result);
        return result;
    }

    private String retrieveTheCommonName(final String inSubjectDN) {
        boolean commonNameFound = false;
        String tempCommonName = null;
        final StringTokenizer st = new StringTokenizer(inSubjectDN, ",");

        while (!commonNameFound && st.hasMoreTokens()) {
            final String token = st.nextToken();
            if (isTokenCommonName(token)) {
                commonNameFound = true;
                tempCommonName = token;
            }
        }
        return tempCommonName;
    }

    private String retrieveTheEDIPI(final String commonName) {
        boolean found = false;
        String tempEDIPI = null;
        final StringTokenizer st = new StringTokenizer(commonName, ".");

        while (!found && st.hasMoreTokens()) {
            final String token = st.nextToken();
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
    private boolean isTokenCommonName(final String inToken) {
        final StringTokenizer st = new StringTokenizer(inToken, "=");
        return st.nextToken().equals(COMMON_NAME_VAR);
    }

    private boolean isTokenEDIPI(final String inToken) {
        return inToken.length() == EDIPI_LENGTH && NumberUtils.isCreatable(inToken);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}

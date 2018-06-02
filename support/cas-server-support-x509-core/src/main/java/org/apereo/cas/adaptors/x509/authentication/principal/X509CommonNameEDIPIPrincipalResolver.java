package org.apereo.cas.adaptors.x509.authentication.principal;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.services.persondir.IPersonAttributeDao;
import java.security.cert.X509Certificate;
import java.util.StringTokenizer;
import lombok.NoArgsConstructor;

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

    public X509CommonNameEDIPIPrincipalResolver(final IPersonAttributeDao attributeRepository, final PrincipalFactory principalFactory,
                                                final boolean returnNullIfNoAttributes, final String principalAttributeName) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        final var subjectDn = certificate.getSubjectDN().getName();
        LOGGER.debug("Creating principal based on subject DN [{}]", subjectDn);
        if (StringUtils.isBlank(subjectDn)) {
            return null;
        }
        final var commonName = retrieveTheCommonName(subjectDn);
        if (StringUtils.isBlank(commonName)) {
            return null;
        }
        final var result = retrieveTheEDIPI(commonName);
        LOGGER.debug("Final principal id extracted from [{}] is [{}]", subjectDn, result);
        return result;
    }

    private String retrieveTheCommonName(final String inSubjectDN) {
        var commonNameFound = false;
        String tempCommonName = null;
        final var st = new StringTokenizer(inSubjectDN, ",");
        while (!commonNameFound && st.hasMoreTokens()) {
            final var token = st.nextToken();
            if (isTokenCommonName(token)) {
                commonNameFound = true;
                tempCommonName = token;
            }
        }
        return StringUtils.remove(tempCommonName, COMMON_NAME_VAR + '=');
    }

    private String retrieveTheEDIPI(final String commonName) {
        var found = false;
        String tempEDIPI = null;
        final var st = new StringTokenizer(commonName, ".");
        while (!found && st.hasMoreTokens()) {
            final var token = st.nextToken();
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
        final var st = new StringTokenizer(inToken, "=");
        return st.nextToken().equals(COMMON_NAME_VAR);
    }

    private boolean isTokenEDIPI(final String inToken) {
        return inToken.length() == EDIPI_LENGTH && NumberUtils.isCreatable(inToken);
    }
}

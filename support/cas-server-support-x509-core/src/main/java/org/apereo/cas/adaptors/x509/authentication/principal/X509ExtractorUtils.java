package org.apereo.cas.adaptors.x509.authentication.principal;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Methods for extracting values from certificates to use as principal IDs or person attributes.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @author Hal Deadman
 * @since 6.4.0
 */
@UtilityClass
public class X509ExtractorUtils {

    private static final String COMMON_NAME_VAR = "CN";

    private static final int EDIPI_LENGTH = 10;

    /**
     * Subject Alternative Name field id for RFC822 Email Type.
     */
    private static int SAN_RFC822_EMAIL_TYPE = 1;

    /**
     * Extract common name (CN) from subject DN.
     * @param inSubjectDN subject distinguished name
     * @return common name string
     */
    public String retrieveTheCommonName(final String inSubjectDN) {
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

    /**
     * Retrieve EDIPI number from common name.
     * @param commonName Value of CN field.
     * @return EDIPI number if found or empty string.
     */
    public String retrieveTheEDIPI(final String commonName) {
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
    private boolean isTokenCommonName(final String inToken) {
        val st = new StringTokenizer(inToken, "=");
        return st.nextToken().equals(COMMON_NAME_VAR);
    }

    private boolean isTokenEDIPI(final String inToken) {
        return inToken.length() == EDIPI_LENGTH && NumberUtils.isCreatable(inToken);
    }

    /**
     * Get Email Address.
     *
     * @param subjectAltNames list of subject alternative name values encoded as collection of Lists with two elements in each List containing type and value.
     * @return String email address or null if the item passed in is not type 1 (rfc822Name)
     * as expected to be returned by implementation of {@code X509Certificate.html#getSubjectAlternativeNames}
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()">
     * X509Certificate#getSubjectAlternativeNames</a>
     */
    public String getRFC822EmailAddress(final Collection<List<?>> subjectAltNames) {
        if (subjectAltNames == null) {
            return null;
        }
        val email = subjectAltNames
                .stream()
                .filter(s -> s.size() == 2 && (Integer) s.get(0) == SAN_RFC822_EMAIL_TYPE)
                .findFirst();
        return email.map(objects -> (String) objects.get(1)).orElse(null);
    }
}

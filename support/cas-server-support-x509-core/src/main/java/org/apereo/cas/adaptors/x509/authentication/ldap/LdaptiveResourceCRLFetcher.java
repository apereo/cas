package org.apereo.cas.adaptors.x509.authentication.ldap;

import org.apereo.cas.adaptors.x509.authentication.ResourceCRLFetcher;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LdapUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;

/**
 * Fetches a CRL from an LDAP instance.
 *
 * @author Daniel Fisher
 * @since 4.1
 */
@Slf4j
@RequiredArgsConstructor
public class LdaptiveResourceCRLFetcher extends ResourceCRLFetcher {

    /**
     * The connection config to prep for connections.
     **/
    private final ConnectionConfig connectionConfig;

    /**
     * Search operation that looks for the attribute.
     */
    private final SearchOperation searchOperation;

    private final String certificateAttribute;

    @Override
    public X509CRL fetch(final Resource crl) throws Exception {
        if (LdapUtils.isLdapConnectionUrl(crl.toString())) {
            return fetchCRLFromLdap(crl);
        }
        return super.fetch(crl);
    }

    @Override
    public X509CRL fetch(final URI crl) throws Exception {
        if (LdapUtils.isLdapConnectionUrl(crl)) {
            return fetchCRLFromLdap(crl);
        }
        return super.fetch(crl);
    }

    @Override
    public X509CRL fetch(final URL crl) throws Exception {
        if (LdapUtils.isLdapConnectionUrl(crl)) {
            return fetchCRLFromLdap(crl);
        }
        return super.fetch(crl);
    }

    @Override
    public X509CRL fetch(final String crl) throws Exception {
        if (LdapUtils.isLdapConnectionUrl(crl)) {
            return fetchCRLFromLdap(crl);
        }
        return super.fetch(crl);
    }

    /**
     * Downloads a CRL from given LDAP url.
     *
     * @param r the resource that is the ldap url.
     * @return the x 509 cRL
     * @throws Exception the exception
     */
    protected X509CRL fetchCRLFromLdap(final Object r) throws Exception {
        try {
            val ldapURL = r.toString();
            LOGGER.debug("Fetching CRL from ldap [{}]", ldapURL);

            val result = performLdapSearch(ldapURL);
            if (result.isSuccess()) {
                val entry = result.getEntry();
                val attribute = entry.getAttribute(this.certificateAttribute);

                if (attribute.isBinary()) {
                    LOGGER.debug("Located entry [{}]. Retrieving first attribute [{}]", entry, attribute);
                    return fetchX509CRLFromAttribute(attribute);
                }
                LOGGER.warn("Found certificate attribute [{}] but it is not marked as a binary attribute", this.certificateAttribute);
            }

            LOGGER.debug("Failed to execute the search [{}]", result);
            throw new CertificateException("Failed to establish a connection ldap and search.");

        } catch (final LdapException e) {
            LOGGER.error(e.getMessage(), e);
            throw new CertificateException(e.getMessage());
        }
    }


    /**
     * Gets x509 cRL from attribute. Retrieves the binary attribute value,
     * decodes it to base64, and fetches it as a byte-array resource.
     *
     * @param attribute the attribute, which may be null if it's not found
     * @return the x 509 cRL from attribute
     * @throws Exception the exception
     */
    protected X509CRL fetchX509CRLFromAttribute(final LdapAttribute attribute) throws Exception {
        if (attribute != null && attribute.isBinary()) {
            val val = attribute.getBinaryValue();
            if (val == null || val.length == 0) {
                throw new CertificateException("Empty attribute. Can not download CRL from ldap");
            }
            val decoded64 = EncodingUtils.decodeBase64(val);
            if (decoded64 == null) {
                throw new CertificateException("Could not decode the attribute value to base64");
            }
            LOGGER.trace("Retrieved CRL from ldap as byte array decoded in base64. Fetching...");
            return super.fetch(new ByteArrayResource(decoded64));
        }
        throw new CertificateException("Attribute not found. Can not retrieve CRL");
    }

    /**
     * Executes an LDAP search against the supplied URL.
     *
     * @param ldapURL to search
     * @return search result
     * @throws LdapException if an error occurs performing the search
     */
    protected SearchResponse performLdapSearch(final String ldapURL) throws LdapException {
        val operation = SearchOperation.copy(this.searchOperation);
        operation.setConnectionFactory(prepareConnectionFactory(ldapURL));
        return operation.execute();
    }

    /**
     * Prepare a new LDAP connection.
     *
     * @param ldapURL the ldap uRL
     * @return connection factory
     */
    protected ConnectionFactory prepareConnectionFactory(final String ldapURL) {
        val cc = ConnectionConfig.copy(this.connectionConfig);
        cc.setLdapUrl(ldapURL);
        return new DefaultConnectionFactory(cc);
    }
}

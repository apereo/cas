package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.security.cert.X509Certificate;

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
public class X509CommonNameEDIPIPrincipalResolver extends AbstractX509PrincipalResolver {

    public X509CommonNameEDIPIPrincipalResolver(final PrincipalResolutionContext context) {
        super(context);
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        val subjectDn = certificate.getSubjectDN().getName();
        LOGGER.debug("Creating principal based on subject DN [{}]", subjectDn);
        if (StringUtils.isBlank(subjectDn)) {
            return getAlternatePrincipal(certificate);
        }
        val commonName = X509ExtractorUtils.retrieveTheCommonName(subjectDn);
        if (StringUtils.isBlank(commonName)) {
            return getAlternatePrincipal(certificate);
        }
        val result = X509ExtractorUtils.retrieveTheEDIPI(commonName);
        if (result.isEmpty()) {
            return getAlternatePrincipal(certificate);
        }
        LOGGER.debug("Final principal id extracted from [{}] is [{}]", subjectDn, result.get());
        return result.get();
    }
}

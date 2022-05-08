package org.apereo.cas.support.saml.sts;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.cxf.sts.request.ReceivedToken;
import org.apache.cxf.sts.token.validator.SAMLTokenValidator;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.opensaml.saml.common.SAMLVersion;

import java.time.Instant;

/**
 * This is {@link SamlTokenValidator}.
 * This class is an extension of the CXF class
 * that allows CAS to support OpenSAML v4 APIs
 * particularly around validation or handling of
 * Conditions. This should be removed in future versions
 * of CXF, v4, where support for OpenSAML v4 exists.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 * @deprecated Since 6.6.0, to be removed when CXF v4 is published.
 */
@Slf4j
@Deprecated(since = "6.6.0")
public class SamlTokenValidator extends SAMLTokenValidator {
    @Override
    protected boolean validateConditions(final SamlAssertionWrapper assertion, final ReceivedToken validateTarget) {
        Instant validFrom = null;
        Instant validTill = null;
        Instant issueInstant = null;
        if (assertion.getSamlVersion().equals(SAMLVersion.VERSION_20)) {
            validFrom = assertion.getSaml2().getConditions().getNotBefore();
            validTill = assertion.getSaml2().getConditions().getNotOnOrAfter();
            issueInstant = assertion.getSaml2().getIssueInstant();
        } else {
            validFrom = assertion.getSaml1().getConditions().getNotBefore();
            validTill = assertion.getSaml1().getConditions().getNotOnOrAfter();
            issueInstant = assertion.getSaml1().getIssueInstant();
        }

        val now = Instant.now();
        if (validFrom != null && validFrom.isAfter(now)) {
            LOGGER.warn("SAML Token condition not met - valid-from is after now");
            return false;
        } else if (validTill != null && validTill.isBefore(now)) {
            LOGGER.warn("SAML Token condition not met- - valid-until is before now");
            validateTarget.setState(ReceivedToken.STATE.EXPIRED);
            return false;
        }

        if (issueInstant != null && issueInstant.isAfter(now)) {
            LOGGER.warn("SAML Token IssueInstant not met - it is after now");
            return false;
        }

        return true;
    }
}

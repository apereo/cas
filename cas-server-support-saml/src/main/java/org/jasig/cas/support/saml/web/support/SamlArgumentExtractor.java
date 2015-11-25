package org.jasig.cas.support.saml.web.support;

import org.apache.commons.lang3.NotImplementedException;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.web.support.AbstractArgumentExtractor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @deprecated As of 4.2, use {@link org.jasig.cas.web.support.DefaultArgumentExtractor}.
 * Retrieve the ticket and artifact based on the SAML 1.1 profile.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Deprecated
@Component("samlArgumentExtractor")
public final class SamlArgumentExtractor extends AbstractArgumentExtractor {

    @Override
    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        throw new NotImplementedException("This operation is not supported. "
                + "The class is deprecated and will be removed in future versions");
    }
}

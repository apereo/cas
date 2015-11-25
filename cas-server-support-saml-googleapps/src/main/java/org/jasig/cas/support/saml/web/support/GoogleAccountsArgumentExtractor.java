package org.jasig.cas.support.saml.web.support;

import org.apache.commons.lang3.NotImplementedException;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.AbstractArgumentExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @deprecated As of 4.2, use {@link org.jasig.cas.web.support.DefaultArgumentExtractor}.
 * Constructs a GoogleAccounts compatible service and provides the public and
 * private keys.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Deprecated
public final class GoogleAccountsArgumentExtractor extends AbstractArgumentExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAccountsArgumentExtractor.class);


    /**
     * Instantiates a new google accounts argument extractor.
     *
     * @param publicKey the public key
     * @param privateKey the private key
     * @param servicesManager the services manager
     */
    public GoogleAccountsArgumentExtractor(final PublicKey publicKey,
                                           final PrivateKey privateKey,
                                           final ServicesManager servicesManager) {
        throw new NotImplementedException("This operation is not supported. "
                + "The class is deprecated and will be removed in future versions");
    }

    @Override
    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        throw new NotImplementedException("This operation is not supported. "
                + "The class is deprecated and will be removed in future versions");
    }

    /**
     * @deprecated As of 4.1. Use Ctors instead.
     * @param privateKey the private key object
     */
    @Deprecated
    public void setPrivateKey(final PrivateKey privateKey) {
        LOGGER.warn("setPrivateKey() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1. Use Ctors instead.
     * @param publicKey the public key object
     */
    @Deprecated
    public void setPublicKey(final PublicKey publicKey) {
        LOGGER.warn("setPublicKey() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1. The behavior is controlled by the service registry instead.
     * Sets an alternate username to send to Google (i.e. fully qualified email address).  Relies on an appropriate
     * attribute available for the user.
     * <p>
     * Note that this is optional and the default is to use the normal identifier.
     *
     * @param alternateUsername the alternate username. This is OPTIONAL.
     */
    @Deprecated
    public void setAlternateUsername(final String alternateUsername) {
        LOGGER.warn("setAlternateUsername() is deprecated and has no effect. Instead use the configuration in service registry.");
    }
}

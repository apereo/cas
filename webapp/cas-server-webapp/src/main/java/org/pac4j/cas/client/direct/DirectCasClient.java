package org.pac4j.cas.client.direct;

import org.jasig.cas.client.util.CommonUtils;
import org.pac4j.cas.authorization.DefaultCasAuthorizationGenerator;
import org.pac4j.cas.client.CasProxyReceptor;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.credentials.authenticator.CasAuthenticator;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.client.DirectClientV2;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.extractor.ParameterExtractor;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.CallbackUrlResolver;
import org.pac4j.core.util.CommonHelper;

/**
 * <p>This class is the direct client to authenticate users on a CAS server for a web application in a stateless way: when trying to access a protected area,
 * the user will be redirected to the CAS server for login and then back directly to this originally requested url.</p>
 *
 * <p>You should generally use the {@link org.pac4j.cas.client.CasClient} instead (this one is very specific and was designed for OAuth / OpenID Connect implementations in the CAS server.</p>
 *
 * <p>The configuration can be defined via the {@link #configuration} object.</p>
 *
 * <p>As no session is meant to be created, this client does not handle CAS logout requests.</p>
 *
 * <p>For proxy support, a {@link CasProxyReceptor} must be defined in the configuration (the corresponding "callback filter" must be enabled)
 * and set to the CAS configuration of this client. In that case, a {@link org.pac4j.cas.profile.CasProxyProfile} will be return
 * (instead of a {@link org.pac4j.cas.profile.CasProfile}) to be able to request proxy tickets.</p>
 *
 * @author Jerome Leleu
 * @since 1.9.2
 */
public class DirectCasClient extends DirectClientV2<TokenCredentials, CasProfile> {

    private CasConfiguration configuration;

    public DirectCasClient() { }

    public DirectCasClient(final CasConfiguration casConfiguration) {
        this.configuration = casConfiguration;
    }

    @Override
    protected TokenCredentials retrieveCredentials(final WebContext context) throws HttpAction {
        init(context);
        try {
            String currentUrl = context.getFullRequestURL();
            String loginUrl = configuration.getLoginUrl();
            final CallbackUrlResolver callbackUrlResolver = configuration.getCallbackUrlResolver();
            if (callbackUrlResolver != null) {
                currentUrl = callbackUrlResolver.compute(currentUrl, context);
                loginUrl = callbackUrlResolver.compute(loginUrl, context);
            }

            final TokenCredentials credentials = getCredentialsExtractor().extract(context);
            if (credentials == null) {
                // redirect to the login page
                final String redirectionUrl = CommonUtils.constructRedirectUrl(loginUrl, CasConfiguration.SERVICE_PARAMETER,
                        currentUrl, configuration.isRenew(), false);
                logger.debug("redirectionUrl: {}", redirectionUrl);
                throw HttpAction.redirect("no ticket -> force redirect to login page", context, redirectionUrl);
            }

            // clean url from ticket parameter
            currentUrl = CommonHelper.substringBefore(currentUrl, "?" + CasConfiguration.TICKET_PARAMETER + "=");
            currentUrl = CommonHelper.substringBefore(currentUrl, "&" + CasConfiguration.TICKET_PARAMETER + "=");
            final CasAuthenticator casAuthenticator = new CasAuthenticator(configuration, currentUrl);
            casAuthenticator.init(context);
            casAuthenticator.validate(credentials, context);

            return credentials;
        } catch (CredentialsException e) {
            logger.error("Failed to retrieve or validate CAS credentials", e);
            return null;
        }
    }

    @Override
    protected void internalInit(final WebContext context) {
        CommonHelper.assertNotNull("configuration", this.configuration);
        CommonHelper.assertTrue(!configuration.isGateway(), "the DirectCasClient can not support gateway to avoid infinite loops");
        configuration.init(context);

        setCredentialsExtractor(new ParameterExtractor(CasConfiguration.TICKET_PARAMETER, true, true, getName()));
        // only a fake one for the initialization as we will build a new one with the current url for each request
        super.setAuthenticator(new CasAuthenticator(configuration, "fake"));
        addAuthorizationGenerator(new DefaultCasAuthorizationGenerator<>());
    }

    public CasConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(CasConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setAuthenticator(final Authenticator authenticator) {
        throw new TechnicalException("You can not set an Authenticator for the DirectCasClient at startup. A new CasAuthenticator is automatically created for each request");
    }

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "configuration", this.configuration);
    }
}

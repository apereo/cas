package org.apereo.cas.services.web;
import static org.apereo.cas.support.oauth.OAuth20Constants.BASE_OAUTH20_URL;
import static org.apereo.cas.support.oauth.OAuth20Constants.CLIENT_ID;
import static org.apereo.cas.support.oauth.OAuth20Constants.REDIRECT_URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.ServiceThemeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ResourceLoader;

//Allow OAuth to use it the theme define in the original theme
public class CasOAuthThemeResolver extends ServiceThemeResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasOAuthThemeResolver.class);

    private CasConfigurationProperties casProperties;
    private ServicesManager servicesManager;

	public CasOAuthThemeResolver(ServicesManager servicesManager, Map<String, String> mobileOverrides,
			AuthenticationServiceSelectionPlan serviceSelectionStrategies, ResourceLoader resourceLoader, CasConfigurationProperties casProperties) {
		super(servicesManager, mobileOverrides, serviceSelectionStrategies, resourceLoader);
		this.casProperties = casProperties;
		this.servicesManager = servicesManager;
	}


    /**
     * The logic is as following:
     * - Check if the service ID is determind to be OAuth handling service
     * - If true, extract the redirect_uri get param from the service ID
     * - Use the redirect_uri to discover the service
     *
     * @param request  the request
     * @param service  the service
     * @param rService the r service
     * @return the string
     */
	@Override
	protected String determineThemeNameToChoose(final HttpServletRequest request,
            final Service service,
            final RegisteredService rService) {
			try {
				
				
				final String oAuthCallbackUrlIdentifier = casProperties.getServer().getPrefix() + BASE_OAUTH20_URL;
				
				String redirectUri = null;

		        String serviceUrl = service.getId();
				

				//Detemind if the service is OAuth
				if(serviceUrl.startsWith(oAuthCallbackUrlIdentifier) ) {
					
					List<NameValuePair> params = URLEncodedUtils.parse(serviceUrl, StandardCharsets.UTF_8);
			
					for (NameValuePair param : params) {
						if(param.getName().equals(REDIRECT_URI)) {
							redirectUri = param.getValue();
						}
					}
					//If redirectUri is not null, find service by redirectUri
					if (redirectUri != null) {
						RegisteredService originalService = servicesManager.findServiceBy(redirectUri);
						String originalTheme = originalService.getTheme();
						if(originalService != null && originalTheme != null) {
							return originalTheme;
						}
						
					}
					
				}
				
				LOGGER.warn("Custom theme [{}] for service [{}] cannot be located. Falling back to default theme...", rService.getTheme(), rService);
			} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return getDefaultThemeName();
	}

	
}



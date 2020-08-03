package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Builder
@Slf4j
public class DelegatedClientIdentityProviderConfigurationFactory {
    /**
     * Endpoint path controlled by this controller to make the redirect.
     */
    public static final String ENDPOINT_URL_REDIRECT = "clientredirect";

    private static final Pattern PAC4J_CLIENT_SUFFIX_PATTERN = Pattern.compile("Client\\d*");

    private final IndirectClient client;

    private final WebContext webContext;

    private final WebApplicationService service;

    private final CasConfigurationProperties casProperties;

    /**
     * Build provider configuration.
     *
     * @return the optional provider
     */
    public Optional<DelegatedClientIdentityProviderConfiguration> resolve() {
        val name = client.getName();
        val matcher = PAC4J_CLIENT_SUFFIX_PATTERN.matcher(client.getClass().getSimpleName());
        val type = matcher.replaceAll(StringUtils.EMPTY).toLowerCase();
        val uriBuilder = UriComponentsBuilder
            .fromUriString(ENDPOINT_URL_REDIRECT)
            .queryParam(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, name);
        val queryParams = new HashMap<String, String>();

        LOGGER.debug("Request parameters are [{}]", webContext.getRequestParameters());
        if (service != null) {
            val sourceParam = service.getSource();
            val serviceParam = service.getOriginalUrl();
            LOGGER.debug("Processing service parameter [{}] with value [{}]", sourceParam, serviceParam);
            if (StringUtils.isNotBlank(sourceParam) && StringUtils.isNotBlank(serviceParam)) {
                uriBuilder.queryParam(sourceParam, "{service}");
                queryParams.put(CasProtocolConstants.PARAMETER_SERVICE, serviceParam);
            }
        }

        val methodParam = webContext.getRequestParameter(CasProtocolConstants.PARAMETER_METHOD)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isNotBlank(methodParam)) {
            LOGGER.debug("Processing method parameter [{}] with value [{}]",
                CasProtocolConstants.PARAMETER_METHOD, methodParam);
            uriBuilder.queryParam(CasProtocolConstants.PARAMETER_METHOD, "{method}");
            queryParams.put("method", methodParam);
        }
        LOGGER.debug("Processing locale parameter [{}]", casProperties.getLocale().getParamName());
        val localeParam = webContext.getRequestParameter(casProperties.getLocale().getParamName())
            .map(String::valueOf).orElse(casProperties.getLocale().getDefaultValue());
        if (StringUtils.isNotBlank(localeParam)) {
            LOGGER.debug("Processing locale parameter [{}] with value [{}]",
                casProperties.getLocale().getParamName(), localeParam);
            uriBuilder.queryParam(casProperties.getLocale().getParamName(), "{locale}");
            queryParams.put("locale", localeParam);
        }
        val themeParam = webContext.getRequestParameter(casProperties.getTheme().getParamName())
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isNotBlank(themeParam)) {
            LOGGER.debug("Processing theme parameter [{}] with value [{}]",
                casProperties.getTheme().getParamName(), themeParam);
            uriBuilder.queryParam(casProperties.getTheme().getParamName(), "{theme}");
            queryParams.put("theme", themeParam);
        }
        val redirectUrl = uriBuilder.build(queryParams).toString();
        LOGGER.debug("Final redirect url is [{}]", redirectUrl);
        
        val autoRedirect = (Boolean) client.getCustomProperties()
            .getOrDefault(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT, Boolean.FALSE);
        val p = new DelegatedClientIdentityProviderConfiguration(name, redirectUrl, type, getCssClass(client), autoRedirect);
        return Optional.of(p);
    }

    /**
     * Get a valid CSS class for the given provider name.
     *
     * @param client the client
     * @return the css class
     */
    protected String getCssClass(final BaseClient client) {
        val customProperties = client.getCustomProperties();
        if (customProperties != null && customProperties.containsKey(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_CSS_CLASS)) {
            val css = customProperties.get(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_CSS_CLASS).toString();
            LOGGER.debug("Located custom CSS class [{}] for client [{}]", client, css);
            return css;
        }
        return null;
    }
}

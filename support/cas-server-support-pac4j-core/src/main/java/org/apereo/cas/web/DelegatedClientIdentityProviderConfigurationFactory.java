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

        checkForMethodParameter(uriBuilder, queryParams);
        checkForLocalParameter(uriBuilder, queryParams);
        checkForThemeParameter(uriBuilder, queryParams);

        val redirectUrl = uriBuilder.build(queryParams).toString();
        LOGGER.debug("Final redirect url is [{}]", redirectUrl);

        val autoRedirect = (Boolean) client.getCustomProperties()
            .getOrDefault(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT, Boolean.FALSE);
        val title = (String) client.getCustomProperties()
            .getOrDefault(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_DISPLAY_NAME, name);

        val p = DelegatedClientIdentityProviderConfiguration.builder()
            .name(name)
            .autoRedirect(autoRedirect)
            .redirectUrl(redirectUrl)
            .type(type)
            .title(title)
            .cssClass(getCssClass(client))
            .build();
        return Optional.of(p);
    }

    /**
     * Check for theme parameter.
     *
     * @param uriBuilder  the uri builder
     * @param queryParams the query params
     */
    protected void checkForThemeParameter(final UriComponentsBuilder uriBuilder, final HashMap<String, String> queryParams) {
        webContext.getRequestParameter(casProperties.getTheme().getParamName()).ifPresent(themeParam -> {
            LOGGER.debug("Processing theme parameter [{}] with value [{}]",
                casProperties.getTheme().getParamName(), themeParam);
            uriBuilder.queryParam(casProperties.getTheme().getParamName(), "{theme}");
            queryParams.put("theme", themeParam);
        });
    }

    /**
     * Check for local parameter.
     *
     * @param uriBuilder  the uri builder
     * @param queryParams the query params
     */
    protected void checkForLocalParameter(final UriComponentsBuilder uriBuilder, final HashMap<String, String> queryParams) {
        val localProps = casProperties.getLocale();
        LOGGER.debug("Processing locale parameter [{}]", localProps.getParamName());
        webContext.getRequestParameter(localProps.getParamName()).ifPresent(localeParam -> {
            LOGGER.debug("Processing locale parameter [{}] with value [{}]",
                localProps.getParamName(), localeParam);
            uriBuilder.queryParam(localProps.getParamName(), "{locale}");
            queryParams.put("locale", localeParam);
        });
    }

    /**
     * Check for method parameter.
     *
     * @param uriBuilder  the uri builder
     * @param queryParams the query params
     */
    protected void checkForMethodParameter(final UriComponentsBuilder uriBuilder, final HashMap<String, String> queryParams) {
        webContext.getRequestParameter(CasProtocolConstants.PARAMETER_METHOD).ifPresent(methodParam -> {
            LOGGER.debug("Processing method parameter [{}] with value [{}]",
                CasProtocolConstants.PARAMETER_METHOD, methodParam);
            uriBuilder.queryParam(CasProtocolConstants.PARAMETER_METHOD, "{method}");
            queryParams.put("method", methodParam);
        });
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

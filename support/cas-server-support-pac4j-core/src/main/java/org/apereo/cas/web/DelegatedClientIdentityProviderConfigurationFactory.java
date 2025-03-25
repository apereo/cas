package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;

import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SuperBuilder
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
        val type = matcher.replaceAll(StringUtils.EMPTY).toLowerCase(Locale.ENGLISH);
        val uriBuilder = UriComponentsBuilder
            .fromUriString(ENDPOINT_URL_REDIRECT)
            .queryParam(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, name);
        val queryParams = new HashMap<String, String>();

        LOGGER.trace("Request parameters are [{}]", webContext.getRequestParameters());
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
        LOGGER.trace("Final redirect url is [{}]", redirectUrl);

        val autoRedirect = (DelegationAutoRedirectTypes) client.getCustomProperties()
            .getOrDefault(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE, DelegationAutoRedirectTypes.NONE);
        val title = (String) client.getCustomProperties()
            .getOrDefault(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME, name);
        val cssClass = (String) client.getCustomProperties()
            .getOrDefault(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_CSS_CLASS, StringUtils.EMPTY);
        val providerConfig = DelegatedClientIdentityProviderConfiguration.builder()
            .name(name)
            .autoRedirectType(autoRedirect)
            .redirectUrl(redirectUrl)
            .type(type)
            .title(title)
            .cssClass(cssClass)
            .build();
        return Optional.of(providerConfig);
    }

    protected void checkForThemeParameter(final UriComponentsBuilder uriBuilder, final Map<String, String> queryParams) {
        webContext.getRequestParameter(casProperties.getTheme().getParamName()).ifPresent(themeParam -> {
            LOGGER.trace("Processing theme parameter [{}] with value [{}]",
                casProperties.getTheme().getParamName(), themeParam);
            uriBuilder.queryParam(casProperties.getTheme().getParamName(), "{theme}");
            queryParams.put("theme", themeParam);
        });
    }

    protected void checkForLocalParameter(final UriComponentsBuilder uriBuilder, final Map<String, String> queryParams) {
        val localProps = casProperties.getLocale();
        LOGGER.trace("Processing locale parameter [{}]", localProps.getParamName());
        webContext.getRequestParameter(localProps.getParamName()).ifPresent(localeParam -> {
            LOGGER.debug("Processing locale parameter [{}] with value [{}]",
                localProps.getParamName(), localeParam);
            uriBuilder.queryParam(localProps.getParamName(), "{locale}");
            queryParams.put("locale", localeParam);
        });
    }

    protected void checkForMethodParameter(final UriComponentsBuilder uriBuilder, final Map<String, String> queryParams) {
        webContext.getRequestParameter(CasProtocolConstants.PARAMETER_METHOD).ifPresent(methodParam -> {
            LOGGER.trace("Processing method parameter [{}] with value [{}]",
                CasProtocolConstants.PARAMETER_METHOD, methodParam);
            uriBuilder.queryParam(CasProtocolConstants.PARAMETER_METHOD, "{method}");
            queryParams.put("method", methodParam);
        });
    }
}

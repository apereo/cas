package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Builder
public class DelegatedClientIdentityProviderConfigurationFactory {
    /**
     * Endpoint path controlled by this controller to make the redirect.
     */
    public static final String ENDPOINT_URL_REDIRECT = "clientredirect";

    private static final Pattern PAC4J_CLIENT_SUFFIX_PATTERN = Pattern.compile("Client\\d*");

    private static final Pattern PAC4J_CLIENT_CSS_CLASS_SUBSTITUTION_PATTERN = Pattern.compile("\\W");

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

        if (service != null) {
            val sourceParam = service.getSource();
            val serviceParam = service.getOriginalUrl();
            if (StringUtils.isNotBlank(sourceParam) && StringUtils.isNotBlank(serviceParam)) {
                uriBuilder.queryParam(sourceParam, serviceParam);
            }
        }

        val methodParam = webContext.getRequestParameter(CasProtocolConstants.PARAMETER_METHOD)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isNotBlank(methodParam)) {
            uriBuilder.queryParam(CasProtocolConstants.PARAMETER_METHOD, methodParam);
        }
        val localeParam = webContext.getRequestParameter(casProperties.getLocale().getParamName())
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isNotBlank(localeParam)) {
            uriBuilder.queryParam(casProperties.getLocale().getParamName(), localeParam);
        }
        val themeParam = webContext.getRequestParameter(casProperties.getTheme().getParamName())
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isNotBlank(themeParam)) {
            uriBuilder.queryParam(casProperties.getTheme().getParamName(), themeParam);
        }
        val redirectUrl = uriBuilder.toUriString();
        val autoRedirect = (Boolean) client.getCustomProperties()
            .getOrDefault(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT, Boolean.FALSE);
        val p = new DelegatedClientIdentityProviderConfiguration(name, redirectUrl, type, getCssClass(name), autoRedirect);
        return Optional.of(p);
    }

    /**
     * Get a valid CSS class for the given provider name.
     *
     * @param name Name of the provider
     * @return the css class
     */
    protected String getCssClass(final String name) {
        var computedCssClass = "fa fa-lock";
        if (StringUtils.isNotBlank(name)) {
            computedCssClass = computedCssClass.concat(' ' + PAC4J_CLIENT_CSS_CLASS_SUBSTITUTION_PATTERN.matcher(name)
                .replaceAll("-"));
        }
        LOGGER.trace("CSS class for [{}] is [{}]", name, computedCssClass);
        return computedCssClass;
    }
}

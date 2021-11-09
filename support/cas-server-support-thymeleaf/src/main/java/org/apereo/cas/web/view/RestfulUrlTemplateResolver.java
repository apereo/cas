package org.apereo.cas.web.view;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ThemeResolver;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link RestfulUrlTemplateResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class RestfulUrlTemplateResolver extends ThemeFileTemplateResolver {

    public RestfulUrlTemplateResolver(final CasConfigurationProperties casProperties,
                                      final ThemeResolver themeResolver) {
        super(casProperties, themeResolver);
    }

    @Override
    protected ITemplateResource computeTemplateResource(final IEngineConfiguration configuration,
                                                        final String ownerTemplate,
                                                        final String template,
                                                        final String resourceName,
                                                        final String characterEncoding,
                                                        final Map<String, Object> templateResolutionAttributes) {
        val rest = casProperties.getView().getRest();
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val themeName = this.themeResolver.resolveThemeName(request);
        val headers = new LinkedHashMap<String, Object>();
        headers.put("owner", ownerTemplate);
        headers.put("template", template);
        headers.put("resource", resourceName);

        if (StringUtils.isNotBlank(themeName)) {
            headers.put("theme", themeName);
        }

        headers.put("locale", request.getLocale().getCountry());
        headers.putAll(HttpRequestUtils.getRequestHeaders(request));
        headers.putAll(rest.getHeaders());

        HttpResponse response = null;
        try {
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.valueOf(rest.getMethod().toUpperCase().trim()))
                .url(rest.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            val statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return new StringTemplateResource(result);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }

        return super.computeTemplateResource(configuration, ownerTemplate, template, resourceName,
            characterEncoding, templateResolutionAttributes);
    }
}

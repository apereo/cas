package org.apereo.cas.web.view;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpStatus;
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

    public RestfulUrlTemplateResolver(final CasConfigurationProperties casProperties) {
        super(casProperties);
    }

    @Override
    protected ITemplateResource computeTemplateResource(final IEngineConfiguration configuration,
                                                        final String ownerTemplate,
                                                        final String template,
                                                        final String resourceName,
                                                        final String characterEncoding,
                                                        final Map<String, Object> templateResolutionAttributes) {
        val rest = casProperties.getView().getRest();
        val themeName = getCurrentTheme();

        val headers = new LinkedHashMap<String, Object>();
        headers.put("owner", ownerTemplate);
        headers.put("template", template);
        headers.put("resource", resourceName);

        if (StringUtils.isNotBlank(themeName)) {
            headers.put("theme", themeName);
        }

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
        if (request != null) {
            headers.put("locale", request.getLocale().getCountry());
            headers.putAll(HttpRequestUtils.getRequestHeaders(request));
        }
        HttpResponse response = null;
        try {
            response = HttpUtils.execute(rest.getUrl(), rest.getMethod(), rest.getBasicAuthUsername(), rest.getBasicAuthPassword(), headers);
            val statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return new StringTemplateResource(result);
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }

        return super.computeTemplateResource(configuration, ownerTemplate, template, resourceName,
            characterEncoding, templateResolutionAttributes);
    }
}

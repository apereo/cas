package org.apereo.cas.config;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.List;

/**
 * This is {@link CasSwaggerAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Discovery)
@AutoConfiguration
public class CasSwaggerAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSwaggerEndpointConfigurer")
    public CasWebSecurityConfigurer<Void> casSwaggerEndpointConfigurer(
        final SwaggerUiConfigProperties swaggerUiConfigProperties,
        final SpringDocConfigProperties springDocConfigProperties) {
        return new CasWebSecurityConfigurer<>() {
            @Override
            public List<String> getIgnoredEndpoints() {
                val apiDocs = StringUtils.defaultIfBlank(springDocConfigProperties.getApiDocs().getPath(), "/v3/api-docs");
                return List.of(apiDocs, "/swagger-ui/", "/swagger-ui.html");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "casSwaggerOpenApi")
    public OpenAPI casSwaggerOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Apereo CAS Swagger API Documentation")
                .description("Apereo CAS Swagger API Documentation")
                .version(CasVersion.asString())
                .license(new License().name("Apache 2.0")
                    .url("https://github.com/apereo/cas/blob/master/LICENSE")))
            .externalDocs(new ExternalDocumentation()
                .description("Apereo CAS Documentation")
                .url("https://apereo.github.io/cas"));
    }
}

package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.tracing.LocalTraceStore;
import org.apereo.cas.tracing.OtelLocalSpanExporter;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasTracingOtelAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Tracing, module = "otel")
@AutoConfiguration
public class CasTracingOtelAutoConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "otelLocalSpanExporter")
    public SpanExporter otelLocalSpanExporter(@Qualifier(LocalTraceStore.BEAN_NAME)
                                              final LocalTraceStore localTraceStore) {
        return new OtelLocalSpanExporter(localTraceStore);
    }
}

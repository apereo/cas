package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link CasJaegerAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "jaeger")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
public class CasJaegerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "jaegerGrpcHttpSpanExporter")
    public SpanExporter jaegerGrpcHttpSpanExporter(
        @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {

        val jaeger = casProperties.getMonitor().getJaeger();
        val trustManager = Objects.requireNonNull(Arrays.stream(casSslContext.getTrustManagers())
            .filter(X509TrustManager.class::isInstance)
            .map(X509TrustManager.class::cast)
            .findFirst()
            .orElse(null));
        return OtlpGrpcSpanExporter.builder()
            .setTimeout(jaeger.getTimeout())
            .setConnectTimeout(jaeger.getConnectTimeout())
            .setMemoryMode(MemoryMode.valueOf(jaeger.getMemoryMode()))
            .setRetryPolicy(RetryPolicy.builder().setMaxAttempts(jaeger.getMaxRetryAttempts()).build())
            .setSslContext(casSslContext.getSslContext(), trustManager)
            .setHeaders(jaeger::getHeaders)
            .setEndpoint(jaeger.getEndpoint())
            .build();
    }
}

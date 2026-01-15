package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.BaseConverters;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.throttle.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.support.MongoDbThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link CasMongoDbThrottlingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Throttling, module = "mongo")
@AutoConfiguration
public class CasMongoDbThrottlingAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(
        final CasConfigurationProperties casProperties,
        @Qualifier("authenticationThrottlingConfigurationContext")
        final ThrottledSubmissionHandlerConfigurationContext authenticationThrottlingConfigurationContext,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        val mongo = casProperties.getAudit().getMongo();
        val factory = new MongoDbConnectionFactory(List.of(new AuditActionContextConverter()), casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return new MongoDbThrottledSubmissionHandlerInterceptorAdapter(authenticationThrottlingConfigurationContext, mongoTemplate, mongo.getCollection());
    }

    private static final class AuditActionContextConverter extends BaseConverters.NullConverter<Document, AuditActionContext> {
        private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false).build().toObjectMapper();

        @Override
        public AuditActionContext convert(@NonNull final Document document) {
            return FunctionUtils.doUnchecked(() ->
                MAPPER.readValue(document.toJson(JsonWriterSettings.builder()
                    .outputMode(JsonMode.RELAXED)
                    .dateTimeConverter((value, writer) -> writer.writeString(DateTimeUtils.localDateOf(value).toString()))
                    .build()), AuditActionContext.class));
        }
    }
}

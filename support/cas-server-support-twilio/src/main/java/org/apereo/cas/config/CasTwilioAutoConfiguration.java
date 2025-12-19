package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.call.PhoneCallOperator;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.call.TwilioPhoneCallOperator;
import org.apereo.cas.support.sms.TwilioSmsSender;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasTwilioAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "twilio")
@AutoConfiguration
public class CasTwilioAutoConfiguration {
    private static final BeanCondition TWILIO_CONDITION = BeanCondition
        .on("cas.sms-provider.twilio.account-id")
        .and("cas.sms-provider.twilio.token");


    @Configuration(value = "TwilioSmsConfiguration", proxyBeanMethods = false)
    static class TwilioSmsConfiguration {
        private static final BeanCondition TWILIO_SMS_CONDITION = BeanCondition
            .on("cas.sms-provider.twilio.enabled").isTrue().evenIfMissing();

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SmsSender smsSender(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(SmsSender.class)
                .when(TWILIO_CONDITION.given(applicationContext.getEnvironment()))
                .and(TWILIO_SMS_CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new TwilioSmsSender(casProperties.getSmsProvider().getTwilio()))
                .otherwise(SmsSender::noOp)
                .get();
        }
    }

    @Configuration(value = "TwilioPhoneCallConfiguration", proxyBeanMethods = false)
    static class TwilioPhoneCallConfiguration {
        private static final BeanCondition TWILIO_PHONE_CONDITION = BeanCondition
            .on("cas.sms-provider.twilio.phone-calls-enabled").isTrue();
        
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PhoneCallOperator phoneCallOperator(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(PhoneCallOperator.class)
                .when(TWILIO_CONDITION.given(applicationContext.getEnvironment()))
                .and(TWILIO_PHONE_CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new TwilioPhoneCallOperator(casProperties.getSmsProvider().getTwilio()))
                .otherwise(PhoneCallOperator::noOp)
                .get();
        }
    }
}

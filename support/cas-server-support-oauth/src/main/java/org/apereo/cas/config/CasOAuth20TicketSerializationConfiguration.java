package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20DefaultCode;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceToken;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceUserCode;
import org.apereo.cas.ticket.device.OAuth20DeviceToken;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCode;
import org.apereo.cas.ticket.refreshtoken.OAuth20DefaultRefreshToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.io.Serial;

/**
 * This is {@link CasOAuth20TicketSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OAuth)
@Configuration(value = "CasOAuth20TicketSerializationConfiguration", proxyBeanMethods = false)
class CasOAuth20TicketSerializationConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketSerializationExecutionPlanConfigurer oauthTicketSerializationExecutionPlanConfigurer() {
        return plan -> {
            plan.registerTicketSerializer(new OAuthCodeTicketStringSerializer());
            plan.registerTicketSerializer(new AccessTokenTicketStringSerializer());
            plan.registerTicketSerializer(new RefreshTokenTicketStringSerializer());
            plan.registerTicketSerializer(new DeviceTokenTicketStringSerializer());
            plan.registerTicketSerializer(new DeviceUserCodeTicketStringSerializer());

            plan.registerTicketSerializer(OAuth20Code.class.getName(), new OAuthCodeTicketStringSerializer());
            plan.registerTicketSerializer(OAuth20AccessToken.class.getName(), new AccessTokenTicketStringSerializer());
            plan.registerTicketSerializer(OAuth20RefreshToken.class.getName(), new RefreshTokenTicketStringSerializer());
            plan.registerTicketSerializer(OAuth20DeviceToken.class.getName(), new DeviceTokenTicketStringSerializer());
            plan.registerTicketSerializer(OAuth20DeviceUserCode.class.getName(), new DeviceUserCodeTicketStringSerializer());
        };
    }

    private static final class OAuthCodeTicketStringSerializer extends AbstractJacksonBackedStringSerializer<OAuth20DefaultCode> {
        @Serial
        private static final long serialVersionUID = -2198623586274810263L;

        OAuthCodeTicketStringSerializer() {
            super(MINIMAL_PRETTY_PRINTER);
        }
        
        @Override
        public Class<OAuth20DefaultCode> getTypeToSerialize() {
            return OAuth20DefaultCode.class;
        }
    }

    private static final class AccessTokenTicketStringSerializer extends AbstractJacksonBackedStringSerializer<OAuth20DefaultAccessToken> {
        @Serial
        private static final long serialVersionUID = -2198623586274810263L;

        AccessTokenTicketStringSerializer() {
            super(MINIMAL_PRETTY_PRINTER);
        }

        @Override
        public Class<OAuth20DefaultAccessToken> getTypeToSerialize() {
            return OAuth20DefaultAccessToken.class;
        }
    }

    private static final class RefreshTokenTicketStringSerializer extends AbstractJacksonBackedStringSerializer<OAuth20DefaultRefreshToken> {
        @Serial
        private static final long serialVersionUID = -2198623586274810263L;

        RefreshTokenTicketStringSerializer() {
            super(MINIMAL_PRETTY_PRINTER);
        }

        @Override
        public Class<OAuth20DefaultRefreshToken> getTypeToSerialize() {
            return OAuth20DefaultRefreshToken.class;
        }
    }

    private static final class DeviceTokenTicketStringSerializer extends AbstractJacksonBackedStringSerializer<OAuth20DefaultDeviceToken> {
        @Serial
        private static final long serialVersionUID = -2198623586274810263L;

        DeviceTokenTicketStringSerializer() {
            super(MINIMAL_PRETTY_PRINTER);
        }


        @Override
        public Class<OAuth20DefaultDeviceToken> getTypeToSerialize() {
            return OAuth20DefaultDeviceToken.class;
        }
    }

    private static final class DeviceUserCodeTicketStringSerializer extends AbstractJacksonBackedStringSerializer<OAuth20DefaultDeviceUserCode> {
        @Serial
        private static final long serialVersionUID = -2198623586274810263L;

        DeviceUserCodeTicketStringSerializer() {
            super(MINIMAL_PRETTY_PRINTER);
        }

        @Override
        public Class<OAuth20DefaultDeviceUserCode> getTypeToSerialize() {
            return OAuth20DefaultDeviceUserCode.class;
        }
    }
}

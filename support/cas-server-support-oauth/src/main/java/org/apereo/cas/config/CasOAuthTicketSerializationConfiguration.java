package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenImpl;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeImpl;
import org.apereo.cas.ticket.device.DeviceToken;
import org.apereo.cas.ticket.device.DeviceTokenImpl;
import org.apereo.cas.ticket.device.DeviceUserCode;
import org.apereo.cas.ticket.device.DeviceUserCodeImpl;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenImpl;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasOAuthTicketSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casOAuthTicketSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthTicketSerializationConfiguration {

    @Bean
    public TicketSerializationExecutionPlanConfigurer oauthTicketSerializationExecutionPlanConfigurer() {
        return plan -> {
            plan.registerTicketSerializer(new OAuthCodeTicketStringSerializer());
            plan.registerTicketSerializer(new AccessTokenTicketStringSerializer());
            plan.registerTicketSerializer(new RefreshTokenTicketStringSerializer());
            plan.registerTicketSerializer(new DeviceTokenTicketStringSerializer());
            plan.registerTicketSerializer(new DeviceUserCodeTicketStringSerializer());

            plan.registerTicketSerializer(OAuthCode.class.getName(), new OAuthCodeTicketStringSerializer());
            plan.registerTicketSerializer(AccessToken.class.getName(), new AccessTokenTicketStringSerializer());
            plan.registerTicketSerializer(RefreshToken.class.getName(), new RefreshTokenTicketStringSerializer());
            plan.registerTicketSerializer(DeviceToken.class.getName(), new DeviceTokenTicketStringSerializer());
            plan.registerTicketSerializer(DeviceUserCode.class.getName(), new DeviceUserCodeTicketStringSerializer());
        };
    }

    private static class OAuthCodeTicketStringSerializer extends AbstractJacksonBackedStringSerializer<OAuthCodeImpl> {
        private static final long serialVersionUID = -2198623586274810263L;

        @Override
        public Class<OAuthCodeImpl> getTypeToSerialize() {
            return OAuthCodeImpl.class;
        }
    }

    private static class AccessTokenTicketStringSerializer extends AbstractJacksonBackedStringSerializer<AccessTokenImpl> {
        private static final long serialVersionUID = -2198623586274810263L;

        @Override
        public Class<AccessTokenImpl> getTypeToSerialize() {
            return AccessTokenImpl.class;
        }
    }

    private static class RefreshTokenTicketStringSerializer extends AbstractJacksonBackedStringSerializer<RefreshTokenImpl> {
        private static final long serialVersionUID = -2198623586274810263L;

        @Override
        public Class<RefreshTokenImpl> getTypeToSerialize() {
            return RefreshTokenImpl.class;
        }
    }

    private static class DeviceTokenTicketStringSerializer extends AbstractJacksonBackedStringSerializer<DeviceTokenImpl> {
        private static final long serialVersionUID = -2198623586274810263L;

        @Override
        public Class<DeviceTokenImpl> getTypeToSerialize() {
            return DeviceTokenImpl.class;
        }
    }

    private static class DeviceUserCodeTicketStringSerializer extends AbstractJacksonBackedStringSerializer<DeviceUserCodeImpl> {
        private static final long serialVersionUID = -2198623586274810263L;

        @Override
        public Class<DeviceUserCodeImpl> getTypeToSerialize() {
            return DeviceUserCodeImpl.class;
        }
    }
}

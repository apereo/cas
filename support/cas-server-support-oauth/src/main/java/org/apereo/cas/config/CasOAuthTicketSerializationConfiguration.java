package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.accesstoken.AccessTokenImpl;
import org.apereo.cas.ticket.code.OAuthCodeImpl;
import org.apereo.cas.ticket.device.DeviceTokenImpl;
import org.apereo.cas.ticket.device.DeviceUserCodeImpl;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenImpl;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlan;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasOAuthTicketSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casOAuthTicketSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthTicketSerializationConfiguration implements TicketSerializationExecutionPlanConfigurer {

    @Override
    public void configureTicketSerialization(final TicketSerializationExecutionPlan plan) {
        plan.registerTicketSerializer(new AbstractJacksonBackedStringSerializer<OAuthCodeImpl>() {
            private static final long serialVersionUID = -2198623586274810263L;

            @Override
            public Class<OAuthCodeImpl> getTypeToSerialize() {
                return OAuthCodeImpl.class;
            }
        });
        plan.registerTicketSerializer(new AbstractJacksonBackedStringSerializer<AccessTokenImpl>() {
            private static final long serialVersionUID = -2198623586274810263L;

            @Override
            public Class<AccessTokenImpl> getTypeToSerialize() {
                return AccessTokenImpl.class;
            }
        });
        plan.registerTicketSerializer(new AbstractJacksonBackedStringSerializer<RefreshTokenImpl>() {
            private static final long serialVersionUID = -2198623586274810263L;

            @Override
            public Class<RefreshTokenImpl> getTypeToSerialize() {
                return RefreshTokenImpl.class;
            }
        });
        plan.registerTicketSerializer(new AbstractJacksonBackedStringSerializer<DeviceTokenImpl>() {
            private static final long serialVersionUID = -2198623586274810263L;

            @Override
            public Class<DeviceTokenImpl> getTypeToSerialize() {
                return DeviceTokenImpl.class;
            }
        });
        plan.registerTicketSerializer(new AbstractJacksonBackedStringSerializer<DeviceUserCodeImpl>() {
            private static final long serialVersionUID = -2198623586274810263L;

            @Override
            public Class<DeviceUserCodeImpl> getTypeToSerialize() {
                return DeviceUserCodeImpl.class;
            }
        });
    }
}

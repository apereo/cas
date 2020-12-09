package org.apereo.cas.support.inwebo.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.InweboConsoleAdmin;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.util.ssl.SSLUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.ws.transport.http.HttpsUrlConnectionMessageSender;

import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

/**
 * The Inwebo services configuration.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Configuration("inweboConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class InweboServiceConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @ConditionalOnMissingBean(name = "inweboConsoleAdmin")
    @RefreshScope
    public InweboConsoleAdmin inweboConsoleAdmin() {
        val marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(this.getClass().getPackageName().replaceAll("config", "service.soap"));

        val client = new InweboConsoleAdmin();
        client.setDefaultUri("https://api.myinwebo.com/v2/services/ConsoleAdmin");
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);

        try {
            val messageSender = new HttpsUrlConnectionMessageSender();
            messageSender.setKeyManagers(SSLUtils.buildKeystore(casProperties.getAuthn().getMfa().getInwebo().getClientCertificate()).getKeyManagers());
            val tmFactory = TrustManagerFactory.getInstance("PKIX");
            tmFactory.init((KeyStore) null);
            messageSender.setTrustManagers(tmFactory.getTrustManagers());
            client.setMessageSender(messageSender);
        } catch (final Exception e) {
            throw new RuntimeException("Cannot initialize ConsoleAdmin", e);
        }

        return client;
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboService")
    @RefreshScope
    public InweboService inweboService() {
        return new InweboService(casProperties, inweboConsoleAdmin());
    }
}

package org.apereo.cas.support.inwebo.config;

import org.apereo.cas.authentication.DefaultCasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.InweboConsoleAdmin;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.util.ssl.SSLUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    @Qualifier("casSslContext")
    private ObjectProvider<DefaultCasSSLContext> casSslContext;

    @Bean
    @ConditionalOnMissingBean(name = "inweboConsoleAdmin")
    @RefreshScope
    public InweboConsoleAdmin inweboConsoleAdmin() throws Exception {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();

        val marshaller = new Jaxb2Marshaller();
        val marshallerContext = getClass().getPackageName().replaceAll("config", "service.soap.generated");
        marshaller.setContextPath(marshallerContext);

        val client = new InweboConsoleAdmin(casProperties);
        client.setDefaultUri(inwebo.getConsoleAdminUrl());
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);

        val messageSender = new HttpsUrlConnectionMessageSender();
        messageSender.setKeyManagers(SSLUtils.buildKeystore(inwebo.getClientCertificate()).getKeyManagers());
        if (casSslContext.getIfAvailable() != null) {
            messageSender.setTrustManagers(casSslContext.getObject().getTrustManagers());
        } else {
            val tmFactory = TrustManagerFactory.getInstance("PKIX");
            tmFactory.init((KeyStore) null);
            messageSender.setTrustManagers(tmFactory.getTrustManagers());
        }
        client.setMessageSender(messageSender);
        return client;
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboService")
    @RefreshScope
    public InweboService inweboService() throws Exception {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val sslContext = SSLUtils.buildSSLContext(inwebo.getClientCertificate());
        return new InweboService(casProperties, inweboConsoleAdmin(), sslContext);
    }
}

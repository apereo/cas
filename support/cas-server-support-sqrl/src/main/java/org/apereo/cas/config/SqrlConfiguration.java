package org.apereo.cas.config;

import com.github.dbadia.sqrl.server.SqrlConfig;
import com.github.dbadia.sqrl.server.SqrlServerOperations;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.sqrl.SqrlCallbabckController;
import org.apereo.cas.util.DigestUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * This is {@link SqrlConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("SqrlConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SqrlConfiguration {
    private static final int AES_KEY_SIZE = 16;
    
    @Bean
    public SqrlConfig sqrlConfig() {
        try {
            final SqrlConfig c = new SqrlConfig();
            final byte[] key = DigestUtils.sha("thekey".getBytes("UTF-8"));
            c.setAESKeyBytes(Arrays.copyOf(key, AES_KEY_SIZE));
            c.setBackchannelServletPath("/cas/sqlcallback");
            return c;
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public SqrlServerOperations sqrlServerOperations() {
        return new SqrlServerOperations(sqrlConfig());
    }

    @Bean
    public SqrlCallbabckController sqrlCallbackController() {
        return new SqrlCallbabckController(sqrlConfig(), sqrlServerOperations());
    }
}

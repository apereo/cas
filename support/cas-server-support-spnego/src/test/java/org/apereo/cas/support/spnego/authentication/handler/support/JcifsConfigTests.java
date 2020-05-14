package org.apereo.cas.support.spnego.authentication.handler.support;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JcifsConfigTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Tag("Simple")
public class JcifsConfigTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    @SneakyThrows
    public void verifyKerbSysConfig() {
        val path = new ClassPathResource("kerb5.conf").getFile().getCanonicalPath();
        val loginConf = new ClassPathResource("jaas.conf").getFile().getCanonicalPath();

        val settings = new JcifsConfig.SystemSettings();
        settings.setKerberosDebug("true");
        settings.setKerberosConf("file:" + path);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                settings.initialize(applicationContext, "file:" + loginConf);
            }
        });
    }

    @Test
    @SneakyThrows
    public void verifyJcifsConfig() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                val settings = new JcifsConfig.JcifsSettings();
                settings.setJcifsDomain("DOMAIN");
                settings.setJcifsDomainController("CONTROLLER");
                settings.setJcifsNetbiosCachePolicy(1000);
                settings.setJcifsPassword("PASS");
                settings.setJcifsServicePassword("P@$$");
                settings.setJcifsServicePrincipal("EXAMPLE/Principal");
                settings.setJcifsSocketTimeout(100);
                settings.setJcifsUsername("Principal");
            }
        });
    }
}

package org.apereo.cas.support.spnego.authentication.handler.support;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JcifsConfigTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Spnego")
class JcifsConfigTests {

    @Test
    void verifyKerbSysConfig() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val path = new ClassPathResource("kerb5.conf").getFile().getCanonicalPath();
        val loginConf = new ClassPathResource("jaas.conf").getFile().getCanonicalPath();

        val settings = new JcifsConfig.SystemSettings();
        settings.setKerberosDebug("true");
        settings.setKerberosConf("file:" + path);
        assertDoesNotThrow(() -> settings.initialize(applicationContext, "file:" + loginConf));
    }

    @Test
    void verifyJcifsConfig() {
        assertDoesNotThrow(() -> {
            val settings = new JcifsConfig.JcifsSettings();
            settings.setJcifsDomain("DOMAIN");
            settings.setJcifsDomainController("CONTROLLER");
            settings.setJcifsNetbiosCachePolicy(1000);
            settings.setJcifsPassword("PASS");
            settings.setJcifsServicePassword("P@$$");
            settings.setJcifsServicePrincipal("EXAMPLE/Principal");
            settings.setJcifsSocketTimeout(100);
            settings.setJcifsUsername("Principal");
        });
    }
}

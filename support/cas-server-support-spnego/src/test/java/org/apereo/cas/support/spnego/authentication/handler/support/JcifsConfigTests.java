package org.apereo.cas.support.spnego.authentication.handler.support;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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
public class JcifsConfigTests {

    @Test
    public void verifyKerbSysConfig() throws Exception {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

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

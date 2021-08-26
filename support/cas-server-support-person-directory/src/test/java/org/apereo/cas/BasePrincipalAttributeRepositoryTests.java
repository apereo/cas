package org.apereo.cas;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryGroovyConfiguration;
import org.apereo.cas.config.CasPersonDirectoryGrouperConfiguration;
import org.apereo.cas.config.CasPersonDirectoryJdbcConfiguration;
import org.apereo.cas.config.CasPersonDirectoryJsonConfiguration;
import org.apereo.cas.config.CasPersonDirectoryLdapConfiguration;
import org.apereo.cas.config.CasPersonDirectoryRestConfiguration;
import org.apereo.cas.config.CasPersonDirectoryScriptedConfiguration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BasePrincipalAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public abstract class BasePrincipalAttributeRepositoryTests {
    @ImportAutoConfiguration({
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasPersonDirectoryConfiguration.class,
        CasPersonDirectoryJdbcConfiguration.class,
        CasPersonDirectoryLdapConfiguration.class,
        CasPersonDirectoryGrouperConfiguration.class,
        CasPersonDirectoryGroovyConfiguration.class,
        CasPersonDirectoryRestConfiguration.class,
        CasPersonDirectoryJsonConfiguration.class,
        CasPersonDirectoryScriptedConfiguration.class,
        CasCoreUtilConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}

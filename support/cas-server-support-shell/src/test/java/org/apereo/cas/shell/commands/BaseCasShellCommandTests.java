package org.apereo.cas.shell.commands;

import org.apereo.cas.config.CasCommandLineShellConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.Shell;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.shell.standard.StandardAPIAutoConfiguration;

/**
 * This is {@link BaseCasShellCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    SpringShellAutoConfiguration.class,
    JLineShellAutoConfiguration.class,
    StandardAPIAutoConfiguration.class,
    CasCommandLineShellConfiguration.class
}, properties = "spring.shell.interactive.enabled=false")
@ComponentScan(basePackages = "org.apereo.cas.shell.commands")
@EnableAutoConfiguration
public abstract class BaseCasShellCommandTests {
    @Autowired
    @Qualifier("shell")
    protected Shell shell;
}

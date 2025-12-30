package org.apereo.cas.shell.commands;

import module java.base;
import org.apereo.cas.CasCommandLineShellApplication;
import org.apereo.cas.config.CasCommandLineShellAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.shell.core.ShellRunner;
import org.springframework.shell.core.autoconfigure.SpringShellAutoConfiguration;

/**
 * This is {@link BaseCasShellCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    SpringShellAutoConfiguration.class,
    CasCoreSamlAutoConfiguration.class,
    CasCommandLineShellAutoConfiguration.class
}, properties = "spring.shell.interactive.enabled=false")
@Import(CasCommandLineShellApplication.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseCasShellCommandTests {
    @Autowired
    @Qualifier("shellRunner")
    protected ShellRunner shellRunner;

    protected @Nullable Object runShellCommand(final Supplier<String> inputProvider) throws Exception {
        shellRunner.run(new String[]{inputProvider.get()});
        return null;
    }
}

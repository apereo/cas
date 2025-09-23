package org.apereo.cas.shell.commands;

import org.apereo.cas.config.CasCommandLineShellAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.Input;
import org.springframework.shell.InputProvider;
import org.springframework.shell.Shell;
import org.springframework.shell.boot.CommandCatalogAutoConfiguration;
import org.springframework.shell.boot.ExitCodeAutoConfiguration;
import org.springframework.shell.boot.JLineAutoConfiguration;
import org.springframework.shell.boot.JLineShellAutoConfiguration;
import org.springframework.shell.boot.ParameterResolverAutoConfiguration;
import org.springframework.shell.boot.ShellContextAutoConfiguration;
import org.springframework.shell.boot.SpringShellAutoConfiguration;
import org.springframework.shell.boot.StandardAPIAutoConfiguration;
import org.springframework.shell.boot.ThemingAutoConfiguration;
import org.springframework.shell.boot.UserConfigAutoConfiguration;
import org.springframework.util.ReflectionUtils;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseCasShellCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    ExitCodeAutoConfiguration.class,
    ParameterResolverAutoConfiguration.class,
    ThemingAutoConfiguration.class,
    UserConfigAutoConfiguration.class,
    JLineAutoConfiguration.class,
    SpringShellAutoConfiguration.class,
    JLineShellAutoConfiguration.class,
    CommandCatalogAutoConfiguration.class,
    StandardAPIAutoConfiguration.class,
    ShellContextAutoConfiguration.class,
    CasCoreSamlAutoConfiguration.class,
    CasCommandLineShellAutoConfiguration.class
}, properties = {
    "spring.main.allow-circular-references=true",
    "spring.shell.interactive.enabled=false"
})
@ComponentScan(basePackages = "org.apereo.cas.shell.commands")
@ExtendWith(CasTestExtension.class)
public abstract class BaseCasShellCommandTests {
    @Autowired
    @Qualifier("shell")
    protected Shell shell;

    protected Object runShellCommand(final InputProvider inputProvider) {
        val method = ReflectionUtils.findMethod(shell.getClass(), "evaluate", Input.class);
        assertDoesNotThrow(() -> Objects.requireNonNull(method).trySetAccessible());
        return assertDoesNotThrow(() -> ReflectionUtils.invokeMethod(Objects.requireNonNull(method), shell, inputProvider.readInput()));
    }
}

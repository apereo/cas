package org.apereo.cas.shell.commands;

import org.apereo.cas.config.CasCommandLineShellConfiguration;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.Input;
import org.springframework.shell.InputProvider;
import org.springframework.shell.Shell;
import org.springframework.shell.boot.JLineShellAutoConfiguration;
import org.springframework.shell.boot.SpringShellAutoConfiguration;
import org.springframework.shell.boot.StandardAPIAutoConfiguration;
import org.springframework.util.ReflectionUtils;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

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
}, properties = {
    "spring.main.allow-circular-references=true",
    "spring.shell.interactive.enabled=false"
})
@ComponentScan(basePackages = "org.apereo.cas.shell.commands")
@EnableAutoConfiguration
public abstract class BaseCasShellCommandTests {
    @Autowired
    @Qualifier("shell")
    protected Shell shell;

    protected Object runShellCommand(final InputProvider inputProvider) throws Exception {
        val method = ReflectionUtils.findMethod(shell.getClass(), "evaluate", Input.class);
        assertDoesNotThrow(() -> Objects.requireNonNull(method).trySetAccessible());
        return assertDoesNotThrow(() -> ReflectionUtils.invokeMethod(Objects.requireNonNull(method), shell, inputProvider.readInput()));
    }
}

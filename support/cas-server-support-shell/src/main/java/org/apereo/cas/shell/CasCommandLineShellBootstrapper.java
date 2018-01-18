package org.apereo.cas.shell;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.shell.cli.CasCommandLineParser;
import org.springframework.shell.Bootstrap;

/**
 * This is {@link CasCommandLineShellBootstrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CasCommandLineShellBootstrapper {

    /**
     * Execute.
     *
     * @param args the args
     */
    @SneakyThrows
    public void execute(final String[] args) {
        CasCommandLineParser.convertToSystemProperties(args);
        Bootstrap.main(new String[]{});
    }
}

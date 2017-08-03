package org.apereo.cas.shell;

import org.apereo.cas.shell.cli.CasCommandLineParser;
import org.springframework.shell.Bootstrap;

/**
 * This is {@link CasCommandLineShellBootstrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasCommandLineShellBootstrapper {

    /**
     * Execute.
     *
     * @param args the args
     */
    public void execute(final String[] args) {
        try {
            CasCommandLineParser.convertToSystemProperties(args);
            Bootstrap.main(new String[]{});
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

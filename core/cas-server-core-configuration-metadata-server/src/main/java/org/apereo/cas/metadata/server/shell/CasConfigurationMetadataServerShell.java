package org.apereo.cas.metadata.server.shell;

import com.google.common.base.Throwables;
import org.apereo.cas.metadata.server.cli.ConfigurationMetadataServerCommandLineParser;
import org.springframework.shell.Bootstrap;

/**
 * This is {@link CasConfigurationMetadataServerShell}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasConfigurationMetadataServerShell {

    /**
     * Execute.
     *
     * @param args the args
     */
    public void execute(final String[] args) {
        try {
            ConfigurationMetadataServerCommandLineParser.convertToSystemProperties(args);
            Bootstrap.main(new String[]{});
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}

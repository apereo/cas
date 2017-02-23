package org.apereo.cas.support.events.config;

import org.apereo.cas.support.events.AbstractCasEvent;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * This is {@link CasConfigurationModifiedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasConfigurationModifiedEvent extends AbstractCasEvent {
    private static final long serialVersionUID = -5738763037210896455L;
    private static final Pattern CONFIG_FILE_PATTERN = Pattern.compile("\\.(properties|yml)", Pattern.CASE_INSENSITIVE);

    private final Path file;
    private boolean override;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source the source
     */
    public CasConfigurationModifiedEvent(final Object source, final Path file) {
        this(source, file, false);
    }

    public CasConfigurationModifiedEvent(final Object source, final boolean override) {
        this(source, null, override);
    }

    public CasConfigurationModifiedEvent(final Object source, final Path file, final boolean override) {
        super(source);
        this.file = file;
        this.override = override;
    }

    private Path getFile() {
        return file;
    }

    public boolean isOverride() {
        return override;
    }

    /**
     * Is eligible for context refresh ?
     *
     * @return the boolean
     */
    public boolean isEligibleForContextRefresh() {
        if (this.override) {
            return true;
        }

        if (getFile() != null) {
            final File file = getFile().toFile();
            return CONFIG_FILE_PATTERN.matcher(file.getName()).find();
        }
        return false;
    }
}

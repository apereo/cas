package org.apereo.cas.support.events.config;

import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * This is {@link CasConfigurationModifiedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasConfigurationModifiedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = -5738763037210896455L;

    private static final Pattern CONFIG_FILE_PATTERN = Pattern.compile("\\.(properties|yml)", Pattern.CASE_INSENSITIVE);

    private final transient Path file;

    private final boolean override;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source the source
     * @param file   the file
     */
    public CasConfigurationModifiedEvent(final Object source, final Path file) {
        this(source, file, false);
    }

    /**
     * Instantiates a new Cas configuration modified event.
     *
     * @param source   the source
     * @param override the override
     */
    public CasConfigurationModifiedEvent(final Object source, final boolean override) {
        this(source, null, override);
    }

    /**
     * Instantiates a new Cas configuration modified event.
     *
     * @param source   the source
     * @param file     the file
     * @param override the override
     */
    public CasConfigurationModifiedEvent(final Object source, final Path file, final boolean override) {
        super(source);
        this.file = file;
        this.override = override;
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
            return CONFIG_FILE_PATTERN.matcher(getFile().toFile().getName()).find();
        }
        return false;
    }
}

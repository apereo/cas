package org.apereo.cas.config;

import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = -5738763037210896455L;

    private static final Pattern CONFIG_FILE_PATTERN = Pattern.compile("\\.(properties|yml)", Pattern.CASE_INSENSITIVE);

    private final transient Path file;

    private final boolean override;

    public CasConfigurationModifiedEvent(final Object source, final Path file, final ClientInfo clientInfo) {
        this(source, file, false, clientInfo);
    }

    public CasConfigurationModifiedEvent(final Object source, final boolean override, final ClientInfo clientInfo) {
        this(source, null, override, clientInfo);
    }

    public CasConfigurationModifiedEvent(final Object source, final Path file, final boolean override, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.file = file;
        this.override = override;
    }

    /**
     * Is eligible for context refresh ?
     *
     * @return true/false
     */
    public boolean isEligibleForContextRefresh() {
        if (this.override) {
            return true;
        }
        val fileName = getFile().toFile().getName();
        return CONFIG_FILE_PATTERN.matcher(fileName).find();
    }
}

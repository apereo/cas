package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;


/**
 * This is {@link CasConfigurationServerBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasConfigurationServerBanner extends AbstractCasBanner {
    @Override
    protected String getTitle() {
        return '\n'
            + "   ____    _    ____     ____             __ _         ____                           \n"
            + "  / ___|  / \\  / ___|   / ___|___  _ __  / _(_) __ _  / ___|  ___ _ ____   _____ _ __ \n"
            + " | |     / _ \\ \\___ \\  | |   / _ \\| '_ \\| |_| |/ _` | \\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|\n"
            + " | |___ / ___ \\ ___) | | |__| (_) | | | |  _| | (_| |  ___) |  __/ |   \\ V /  __/ |   \n"
            + "  \\____/_/   \\_\\____/   \\____\\___/|_| |_|_| |_|\\__, | |____/ \\___|_|    \\_/ \\___|_|   \n"
            + "                                               |___/                                  \n";
    }
}

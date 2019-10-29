package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;


/**
 * This is {@link CasEurekaServerBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasEurekaServerBanner extends AbstractCasBanner {
    @Override
    protected String getTitle() {
        return '\n'
            + "   ____    _    ____    _____               _           ____                           \n"
            + "  / ___|  / \\  / ___|  | ____|   _ _ __ ___| | ____ _  / ___|  ___ _ ____   _____ _ __ \n"
            + " | |     / _ \\ \\___ \\  |  _|| | | | '__/ _ \\ |/ / _` | \\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|\n"
            + " | |___ / ___ \\ ___) | | |__| |_| | | |  __/   < (_| |  ___) |  __/ |   \\ V /  __/ |   \n"
            + "  \\____/_/   \\_\\____/  |_____\\__,_|_|  \\___|_|\\_\\__,_| |____/ \\___|_|    \\_/ \\___|_|   \n"
            + "                                                                                       \n";
    }
}

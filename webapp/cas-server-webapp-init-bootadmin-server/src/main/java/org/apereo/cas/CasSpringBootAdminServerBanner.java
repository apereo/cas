package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;


/**
 * This is {@link CasSpringBootAdminServerBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasSpringBootAdminServerBanner extends AbstractCasBanner {
    @Override
    protected String getTitle() {
        return '\n'
            + "   ____    _    ____    ____              _        _       _           _         ____                           \n"
            + "  / ___|  / \\  / ___|  | __ )  ___   ___ | |_     / \\   __| |_ __ ___ (" + "_)_ __   / ___|  ___ _ ____   _____ _ __ \n"
            + " | |     / _ \\ \\___ \\  |  _ \\ / _ \\ / _ \\| __|   / _ \\ / _` | '_ ` _ \\| | '_ \\  \\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|\n"
            + " | |___ / ___ \\ ___) | | |_) | (_) | (_) | |_   / ___ \\ (_| | | | | | | | | | |  ___) |  __/ |   \\ V /  __/ |   \n"
            + "  \\____/_/   \\_\\____/  |____/ \\___/ \\___/ \\__| /_/   \\_\\__,_|_| |_| |_|_|_| |_| |____/ \\___|_|    \\_/ \\___|_|   \n"
            + "                                                                                                                \n";
    }
}

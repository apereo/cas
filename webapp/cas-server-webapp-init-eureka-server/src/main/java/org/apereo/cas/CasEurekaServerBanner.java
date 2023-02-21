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
    public String getTitle() {
        return """

               ____    _    ____    _____               _           ____                          \s
              / ___|  / \\  / ___|  | ____|   _ _ __ ___| | ____ _  / ___|  ___ _ ____   _____ _ __\s
             | |     / _ \\ \\___ \\  |  _|| | | | '__/ _ \\ |/ / _` | \\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|
             | |___ / ___ \\ ___) | | |__| |_| | | |  __/   < (_| |  ___) |  __/ |   \\ V /  __/ |  \s
              \\____/_/   \\_\\____/  |_____\\__,_|_|  \\___|_|\\_\\__,_| |____/ \\___|_|    \\_/ \\___|_|  \s
                                                                                                  \s
            """;
    }
}

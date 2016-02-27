package org.jasig.cas.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is {@link CasWebApplication}.
 *
 * @author Misagh Moayyed
 * @since x.y.z
 */
@SpringBootApplication(scanBasePackages = { "org.jasig.cas"})
public final class CasWebApplication {

    private CasWebApplication() {
    }

    public static void main(final String[] args) {
        SpringApplication.run(CasWebApplication.class, args);
    }
}

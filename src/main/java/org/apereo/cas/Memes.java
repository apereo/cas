package org.apereo.cas;

import java.security.SecureRandom;

/**
 * This is {@link Memes}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class Memes {
    private final String[] urls;

    public static final Memes PULL_REQUEST_APPROVED = new Memes(
        "https://github.com/apereo/cas/assets/1205228/62f368b6-eb01-408e-963f-5021e266b4e6",
        "https://github.com/apereo/cas/assets/1205228/730395f6-26bf-4ee2-a536-3ec8e5cbd37d",
        "https://github.com/apereo/cas/assets/1205228/c2fec809-7b8c-4748-b349-661c1c6d2f2b",
        "https://github.com/apereo/cas/assets/1205228/d6a7c714-c2c4-4c81-8218-0169aa8bfc5c",
        "https://github.com/apereo/cas/assets/1205228/92be5b5e-7bf6-4485-a1ca-9479d271ab46",
        "https://github.com/apereo/cas/assets/1205228/4ef59720-36cb-461a-8cfd-d08f9ed54613",
        "https://github.com/apereo/cas/assets/1205228/2a18efb5-c53e-4f91-bdaf-c38d57d0e485)",
        "https://github.com/apereo/cas/assets/1205228/9481aa6c-1cd3-4bc9-8a6d-ca98f2bfdc1d)",
        "https://github.com/apereo/cas/assets/1205228/ffa2d297-9095-4602-b38a-5844530ef017)",
        "https://github.com/apereo/cas/assets/1205228/865dee51-9fc6-473d-a68b-f06d60757124)"
    );

    Memes(final String... urls) {
        this.urls = urls;
    }

    public String select() {
        var random = new SecureRandom();
        var randomIndex = random.nextInt(urls.length);
        return urls[randomIndex];
    }
}

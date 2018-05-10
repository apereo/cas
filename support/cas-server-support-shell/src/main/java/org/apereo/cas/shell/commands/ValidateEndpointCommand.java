package org.apereo.cas.shell.commands;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * This is {@link ValidateEndpointCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Service
@Slf4j
public class ValidateEndpointCommand implements CommandMarker {
    /**
     * Validate endpoint.
     *
     * @param url     the url
     * @param proxy   the proxy
     * @param timeout the timeout
     */
    @CliCommand(value = "validate-endpoint", help = "Test connections to an endpoint to verify connectivity, SSL, etc")
    public void validateEndpoint(
        @CliOption(key = {"url"},
            mandatory = true,
            help = "Endpoint URL to test",
            optionContext = "Endpoint URL to test",
            specifiedDefaultValue = "false",
            unspecifiedDefaultValue = "false") final String url,
        @CliOption(key = {"proxy"},
            help = "Proxy address to use when testing the endpoint url",
            specifiedDefaultValue = "",
            unspecifiedDefaultValue = "",
            mandatory = false,
            optionContext = "Proxy address to use when testing the endpoint url") final String proxy,
        @CliOption(key = {"timeout"},
            help = "Timeout to use in milliseconds when testing the url",
            specifiedDefaultValue = "5000",
            unspecifiedDefaultValue = "5000",
            mandatory = false,
            optionContext = "Timeout to use in milliseconds when testing the url") final int timeout) {

        try {
            final URL constructedUrl = new URL(url);
            final URLConnection conn;
            if (StringUtils.isNotBlank(proxy)) {
                final URL proxyUrl = new URL(proxy);
                LOGGER.info("Using proxy address [{}]");
                final InetSocketAddress proxyAddr = new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort());
                conn = constructedUrl.openConnection(new Proxy(Proxy.Type.HTTP, proxyAddr));
            } else {
                conn = constructedUrl.openConnection();
            }
            LOGGER.info("Setting connection timeout to [{}]", timeout);
            conn.setConnectTimeout(timeout);

            LOGGER.info("Trying to connect to [{}]", url);
            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), "UTF-8");
                 BufferedReader in = new BufferedReader(reader)) {
                in.readLine();

                if (conn instanceof HttpURLConnection) {
                    final int code = ((HttpURLConnection) conn).getResponseCode();
                    LOGGER.info("Response status code received: [{}]", code);
                }
                LOGGER.info("Successfully connected to url [{}]", url);
            }
        } catch (final Exception e) {
            LOGGER.info("Could not connect to the host address [{}]", url);
            LOGGER.info("The error is: " + e.getMessage());
            LOGGER.info("Here are the details:");
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

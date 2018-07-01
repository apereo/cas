package org.apereo.cas.shell.commands.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link ValidateEndpointCommand}.
 *
 * @author Misagh Moayyed
 * @author John Gasper
 * @since 5.3.0
 */
@ShellCommandGroup("Utilities")
@ShellComponent
@Slf4j
public class ValidateEndpointCommand {
    /**
     * Validate endpoint.
     *
     * @param url     the url
     * @param proxy   the proxy
     * @param timeout the timeout
     */
    @ShellMethod(key = "validate-endpoint", value = "Test connections to an endpoint to verify connectivity, SSL, etc")
    public void validateEndpoint(
        @ShellOption(value = {"url"},
            help = "Endpoint URL to test") final String url,
        @ShellOption(value = {"proxy"},
            help = "Proxy address to use when testing the endpoint url") final String proxy,
        @ShellOption(value = {"timeout"},
            help = "Timeout to use in milliseconds when testing the url",
            defaultValue = "5000") final int timeout) {

        try {
            LOGGER.info("Trying to connect to [{}]", url);
            final var conn = createConnection(url, proxy);

            LOGGER.info("Setting connection timeout to [{}]", timeout);
            conn.setConnectTimeout(timeout);

            try (var reader = new InputStreamReader(conn.getInputStream(), "UTF-8");
                 var in = new BufferedReader(reader)) {
                in.readLine();

                if (conn instanceof HttpURLConnection) {
                    final var code = ((HttpURLConnection) conn).getResponseCode();
                    LOGGER.info("Response status code received: [{}]", code);
                }
                LOGGER.info("Successfully connected to url [{}]", url);
            }
        } catch (final Exception e) {
            LOGGER.info("Could not connect to the host address [{}]", url);
            LOGGER.info("The error is: {}", e.getMessage());
            LOGGER.info("Here are the details:");
            LOGGER.error(consolidateExceptionMessages(e));
            testBadTlsConnection(url, proxy);
        }
    }

    private URLConnection createConnection(final String url, final String proxy) throws Exception {
        final var constructedUrl = new URL(url);
        final URLConnection conn;

        if (StringUtils.isNotBlank(proxy)) {
            final var proxyUrl = new URL(proxy);
            LOGGER.info("Using proxy address [{}]", proxy);
            final var proxyAddr = new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort());
            conn = constructedUrl.openConnection(new Proxy(Proxy.Type.HTTP, proxyAddr));
        } else {
            conn = constructedUrl.openConnection();
        }

        return conn;
    }

    private String consolidateExceptionMessages(final Throwable throwable) {
        final var stringBuilder = new StringBuilder();

        var pointer = throwable;

        while (pointer != null) {
            stringBuilder.append("  Caused by: ")
                .append(pointer.toString())
                .append(System.getProperty("line.separator"));
            pointer = pointer.getCause();
        }

        return stringBuilder.toString();
    }

    private void testBadTlsConnection(final String url, final String proxy) {
        try {
            final var urlConnection = createConnection(url, proxy);
            if (!(urlConnection instanceof HttpsURLConnection)) {
                LOGGER.info("Not an TLS connection.");
                return;
            }

            final var httpsConnection = (HttpsURLConnection) urlConnection;

            // Setting our own Trust Manager so the connection completes and we can examine the server cert chain.
            httpsConnection.setSSLSocketFactory(getTheAllTrustingSSLContext().getSocketFactory());

            try (var reader = new InputStreamReader(httpsConnection.getInputStream(), "UTF-8")) {
                tlsConnectionReport(httpsConnection);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void tlsConnectionReport(final HttpsURLConnection httpsConnection) {
        final var systemTrustManagers = getSystemTrustManagers();

        final Certificate[] certificates;
        try {
            certificates = httpsConnection.getServerCertificates();
        } catch (final SSLPeerUnverifiedException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        final var serverCertificates =
            Arrays.copyOf(certificates, certificates.length, X509Certificate[].class);

        LOGGER.info("Server provided certs: ");
        for (final var certificate: serverCertificates) {

            String validity;
            try {
                certificate.checkValidity();
                validity = "valid";
            } catch (final Exception e) {
                validity = "invalid: " + e.getMessage();
            }

            LOGGER.info("  subject: {}", certificate.getSubjectDN().getName());
            LOGGER.info("  issuer: {}", certificate.getIssuerDN().getName());
            LOGGER.info("  expiration: {} - {} ({})", certificate.getNotBefore(), certificate.getNotAfter(), validity);
            LOGGER.info("  trust anchor {}", checkTrustedCertStatus(certificate, systemTrustManagers));
            LOGGER.info("---");
        }
    }

    private static X509TrustManager[] getSystemTrustManagers() {
        TrustManagerFactory trustManagerFactory = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }

        LOGGER.info("Detected Truststore: {}", trustManagerFactory.getProvider().getName());
        final List<X509TrustManager> x509TrustManagers = new ArrayList<>();

        for (final var trustManager: trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                final var x509TrustManager = (X509TrustManager) trustManager;
                LOGGER.info("Trusted issuers found: {}", x509TrustManager.getAcceptedIssuers().length);
                x509TrustManagers.add(x509TrustManager);
            }
        }

        return x509TrustManagers.toArray(new X509TrustManager[]{});
    }

    private String checkTrustedCertStatus(final X509Certificate certificate, final X509TrustManager[] trustManagers) {

        for (final var trustManager: trustManagers) {
            for (final var trustedCert: trustManager.getAcceptedIssuers()) {
                try {
                    certificate.verify(trustedCert.getPublicKey());
                    return "Matches found: " + trustedCert.getIssuerDN().getName();
                } catch (final CertificateException | NoSuchAlgorithmException
                    | InvalidKeyException | NoSuchProviderException | SignatureException e) {
                    LOGGER.trace("{}: {}", trustedCert.getIssuerDN().getName(), e.getMessage());
                }
            }
        }

        return "Not matched in trust store (which is expected of the host certificate that is part of a chain)";
    }

    @SneakyThrows
    private SSLContext getTheAllTrustingSSLContext() {
        final var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {

            @Override
            public void checkClientTrusted(final X509Certificate[] xcs, final String string) {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] xcs, final String string) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

        }}, null);
        return sslContext;
    }
}


package org.apereo.cas.shell.commands;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

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
            LOGGER.info("Trying to connect to [{}]", url);
            final URLConnection conn = createConnection(url, proxy);

            LOGGER.info("Setting connection timeout to [{}]", timeout);
            conn.setConnectTimeout(timeout);

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
            LOGGER.info("The error is: {}", e.getMessage());
            LOGGER.info("Here are the details:");
            LOGGER.error(consolidateExceptionMessages(e));
            testBadTlsConnection(url, proxy);
        }
    }

    private URLConnection createConnection(final String url, final String proxy) throws Exception {
        final URL constructedUrl = new URL(url);
        final URLConnection conn;

        if (StringUtils.isNotBlank(proxy)) {
            final URL proxyUrl = new URL(proxy);
            LOGGER.info("Using proxy address [{}]", proxy);
            final InetSocketAddress proxyAddr = new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort());
            conn = constructedUrl.openConnection(new Proxy(Proxy.Type.HTTP, proxyAddr));
        } else {
            conn = constructedUrl.openConnection();
        }

        return conn;
    }

    private String consolidateExceptionMessages(final Throwable throwable) {
        final StringBuilder stringBuilder = new StringBuilder();

        Throwable pointer = throwable;

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
            final URLConnection urlConnection = createConnection(url, proxy);
            if (!(urlConnection instanceof HttpsURLConnection)) {
                LOGGER.info("Not an TLS connection.");
                return;
            }

            final HttpsURLConnection httpsConnection = (HttpsURLConnection) urlConnection;

            //Setting our own Trust Manager so the connection completes and we can examine the server cert chain.
            httpsConnection.setSSLSocketFactory(getTheAllTrustingSSLContext().getSocketFactory());

            try (InputStreamReader reader = new InputStreamReader(httpsConnection.getInputStream(), "UTF-8")) {
                tlsConnectionReport(httpsConnection);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void tlsConnectionReport(final HttpsURLConnection httpsConnection) {
        final X509TrustManager[] systemTrustManagers = getSystemTrustManagers();

        final Certificate[] certificates;
        try {
            certificates = httpsConnection.getServerCertificates();
        } catch (final SSLPeerUnverifiedException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        final X509Certificate[] serverCertificates =
            Arrays.copyOf(certificates, certificates.length, X509Certificate[].class);

        LOGGER.info("Server provided certs: ");
        for (final X509Certificate certificate : serverCertificates) {

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
        final List<X509TrustManager> x509TrustManagers = new ArrayList<X509TrustManager>();

        for (final TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                final X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
                LOGGER.info("Trusted issuers found: {}", x509TrustManager.getAcceptedIssuers().length);
                x509TrustManagers.add(x509TrustManager);
            }
        }

        return x509TrustManagers.toArray(new X509TrustManager[]{});
    }

    private String checkTrustedCertStatus(final X509Certificate certificate, final X509TrustManager[] trustManagers) {

        for (final X509TrustManager trustManager : trustManagers) {
            for (final X509Certificate trustedCert : trustManager.getAcceptedIssuers()) {
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
        final SSLContext sslContext = SSLContext.getInstance("TLS");
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


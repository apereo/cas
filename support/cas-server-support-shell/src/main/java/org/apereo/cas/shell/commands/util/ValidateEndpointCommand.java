package org.apereo.cas.shell.commands.util;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
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
import java.nio.charset.StandardCharsets;
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
    private static X509TrustManager[] getSystemTrustManagers() {
        try {
            var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            LOGGER.info("Detected Truststore: [{}]", trustManagerFactory.getProvider().getName());
            val trustManagers = trustManagerFactory.getTrustManagers();
            val x509TrustManagers = new ArrayList<X509TrustManager>(trustManagers.length);
            for (val trustManager : trustManagers) {
                if (trustManager instanceof X509TrustManager) {
                    val x509TrustManager = (X509TrustManager) trustManager;
                    LOGGER.info("Trusted issuers found: [{}]", x509TrustManager.getAcceptedIssuers().length);
                    x509TrustManagers.add(x509TrustManager);
                }
            }
            return x509TrustManagers.toArray(X509TrustManager[]::new);
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return new X509TrustManager[]{};
    }

    private static URLConnection createConnection(final String url, final String proxy) throws Exception {
        val constructedUrl = new URL(url);
        return FunctionUtils.doIf(StringUtils.isNotBlank(proxy),
            Unchecked.supplier(() -> {
                val proxyUrl = new URL(proxy);
                LOGGER.info("Using proxy address [{}]", proxy);
                val proxyAddr = new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort());
                return constructedUrl.openConnection(new Proxy(Proxy.Type.HTTP, proxyAddr));
            }),
            Unchecked.supplier(constructedUrl::openConnection))
            .get();
    }

    private static String consolidateExceptionMessages(final Throwable throwable) {
        val stringBuilder = new StringBuilder();

        var pointer = throwable;

        while (pointer != null) {
            stringBuilder.append("  Caused by: ")
                .append(pointer.toString())
                .append(System.getProperty("line.separator"));
            pointer = pointer.getCause();
        }

        return stringBuilder.toString();
    }

    private static void testBadTlsConnection(final String url, final String proxy) {
        try {
            val urlConnection = createConnection(url, proxy);
            if (!(urlConnection instanceof HttpsURLConnection)) {
                LOGGER.info("Not an TLS connection.");
                return;
            }

            val httpsConnection = (HttpsURLConnection) urlConnection;

            httpsConnection.setSSLSocketFactory(getTheAllTrustingSSLContext().getSocketFactory());

            try (val reader = new InputStreamReader(httpsConnection.getInputStream(), StandardCharsets.UTF_8)) {
                tlsConnectionReport(httpsConnection);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private static void tlsConnectionReport(final HttpsURLConnection httpsConnection) {
        val systemTrustManagers = getSystemTrustManagers();

        final Certificate[] certificates;
        try {
            certificates = httpsConnection.getServerCertificates();
        } catch (final SSLPeerUnverifiedException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        val serverCertificates =
            Arrays.copyOf(certificates, certificates.length, X509Certificate[].class);

        LOGGER.info("Server provided certs: ");
        for (val certificate : serverCertificates) {
            val validity = FunctionUtils.doAndHandle(
                o -> {
                    certificate.checkValidity();
                    return "valid";
                },
                e -> "invalid: " + e.getMessage()
            ).apply(certificate);

            LOGGER.info("\tsubject: [{}]", certificate.getSubjectDN().getName());
            LOGGER.info("\tissuer: [{}]", certificate.getIssuerDN().getName());
            LOGGER.info("\texpiration: [{}] - [{}] [{}]", certificate.getNotBefore(), certificate.getNotAfter(), validity);
            LOGGER.info("\ttrust anchor [{}]", checkTrustedCertStatus(certificate, systemTrustManagers));
            LOGGER.info("---");
        }
    }

    private static String checkTrustedCertStatus(final X509Certificate certificate, final X509TrustManager[] trustManagers) {

        for (val trustManager : trustManagers) {
            for (val trustedCert : trustManager.getAcceptedIssuers()) {
                try {
                    certificate.verify(trustedCert.getPublicKey());
                    return "Matches found: " + trustedCert.getIssuerDN().getName();
                } catch (final CertificateException | NoSuchAlgorithmException
                    | InvalidKeyException | NoSuchProviderException | SignatureException e) {
                    LOGGER.trace("[{}]: [{}]", trustedCert.getIssuerDN().getName(), e.getMessage());
                }
            }
        }

        return "Not matched in trust store (which is expected of the host certificate that is part of a chain)";
    }

    @SneakyThrows
    @SuppressWarnings("java:S4830")
    private static SSLContext getTheAllTrustingSSLContext() {
        val sslContext = SSLContext.getInstance("TLS");
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

    /**
     * Validate endpoint.
     *
     * @param url     the url
     * @param proxy   the proxy
     * @param timeout the timeout
     * @return true/false
     */
    @ShellMethod(key = "validate-endpoint", value = "Test connections to an endpoint to verify connectivity, SSL, etc")
    public boolean validateEndpoint(
        @ShellOption(value = {"url", "--url"},
            help = "Endpoint URL to test") final String url,
        @ShellOption(value = {"proxy", "--proxy"},
            help = "Proxy address to use when testing the endpoint url",
            defaultValue = StringUtils.EMPTY) final String proxy,
        @ShellOption(value = {"timeout", "--timeout"},
            help = "Timeout to use in milliseconds when testing the url",
            defaultValue = "5000") final int timeout) {

        try {
            LOGGER.info("Trying to connect to [{}]", url);
            val conn = createConnection(url, proxy);

            LOGGER.info("Setting connection timeout to [{}]", timeout);
            conn.setConnectTimeout(timeout);

            try (val reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                 val in = new BufferedReader(reader)) {
                in.readLine();

                if (conn instanceof HttpURLConnection) {
                    val code = ((HttpURLConnection) conn).getResponseCode();
                    LOGGER.info("Response status code received: [{}]", code);
                }
                LOGGER.info("Successfully connected to url [{}]", url);
                return true;
            }
        } catch (final Exception e) {
            LOGGER.info("Could not connect to the host address [{}]", url);
            LOGGER.info("The error is: [{}]", e.getMessage());
            LOGGER.info("Here are the details:");
            LOGGER.error(consolidateExceptionMessages(e));
            testBadTlsConnection(url, proxy);
        }
        return false;
    }
}


package org.apereo.cas.shell.commands.util;

import module java.base;
import org.apereo.cas.shell.commands.CasShellCommand;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import java.net.Proxy;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * This is {@link ValidateEndpointCommand}.
 *
 * @author Misagh Moayyed
 * @author John Gasper
 * @since 5.3.0
 */
@Slf4j
public class ValidateEndpointCommand implements CasShellCommand {
    private static X509TrustManager[] getSystemTrustManagers() throws Exception {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        LOGGER.info("Detected Truststore: [{}]", trustManagerFactory.getProvider().getName());
        val trustManagers = trustManagerFactory.getTrustManagers();
        val x509TrustManagers = new ArrayList<X509TrustManager>(trustManagers.length);
        for (val trustManager : trustManagers) {
            if (trustManager instanceof final X509TrustManager x509TrustManager) {
                LOGGER.info("Trusted issuers found: [{}]", x509TrustManager.getAcceptedIssuers().length);
                x509TrustManagers.add(x509TrustManager);
            }
        }
        return x509TrustManagers.toArray(X509TrustManager[]::new);
    }

    private static URLConnection createConnection(final String url, final String proxy) throws Exception {
        val constructedUrl = new URI(url).toURL();
        return FunctionUtils.doIf(StringUtils.isNotBlank(proxy),
                Unchecked.supplier(() -> {
                    val proxyUrl = new URI(proxy).toURL();
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
                .append(pointer)
                .append(System.lineSeparator());
            pointer = pointer.getCause();
        }

        return stringBuilder.toString();
    }

    private static void testBadTlsConnection(final String url, final String proxy) {
        try {
            val urlConnection = createConnection(url, proxy);
            if (!(urlConnection instanceof final HttpsURLConnection httpsConnection)) {
                LOGGER.info("Not an TLS connection.");
                return;
            }

            httpsConnection.setSSLSocketFactory(getTheAllTrustingSSLContext().getSocketFactory());

            try (val _ = new InputStreamReader(httpsConnection.getInputStream(), StandardCharsets.UTF_8)) {
                tlsConnectionReport(httpsConnection);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    private static void tlsConnectionReport(final HttpsURLConnection httpsConnection) throws Exception {
        val systemTrustManagers = getSystemTrustManagers();

        final Certificate[] certificates;
        try {
            certificates = httpsConnection.getServerCertificates();
        } catch (final SSLPeerUnverifiedException e) {
            LoggingUtils.error(LOGGER, e);
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

    private static String checkTrustedCertStatus(final X509Certificate certificate,
                                                 final X509TrustManager[] trustManagers) {
        for (val trustManager : trustManagers) {
            for (val trustedCert : trustManager.getAcceptedIssuers()) {
                try {
                    certificate.verify(trustedCert.getPublicKey());
                    return "Matches found: " + trustedCert.getIssuerDN().getName();
                } catch (final Exception e) {
                    LOGGER.trace("[{}]: [{}]", trustedCert.getIssuerDN().getName(), e.getMessage());
                }
            }
        }

        return "Not matched in trust store (which is expected of the host certificate that is part of a chain)";
    }

    @SuppressWarnings("java:S4830")
    private static SSLContext getTheAllTrustingSSLContext() {
        return FunctionUtils.doUnchecked(() -> {
            val sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {

                @Override
                public void checkClientTrusted(final X509Certificate[] xcs, final String value) {
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] xcs, final String value) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

            }}, null);
            return sslContext;
        });
    }

    /**
     * Validate endpoint.
     *
     * @param url     the url
     * @param proxy   the proxy
     * @param timeout the timeout
     * @return true /false
     */
    @Command(group = "Utilities", name = "validate-endpoint", description = "Test connections to an endpoint to verify connectivity, SSL, etc")
    public boolean validateEndpoint(
        @Option(
            longName = "url",
            description = "Endpoint URL to test"
        )
        final String url,

        @Option(
            longName = "proxy",
            description = "Proxy address to use when testing the endpoint url",
            defaultValue = StringUtils.EMPTY
        )
        final String proxy,

        @Option(
            longName = "timeout",
            description = "Timeout to use in milliseconds when testing the url",
            defaultValue = "5000"
        )
        final int timeout) {

        try {
            LOGGER.info("Trying to connect to [{}]", url);
            val conn = createConnection(url, proxy);

            LOGGER.info("Setting connection timeout to [{}]", timeout);
            conn.setConnectTimeout(timeout);

            try (val reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                 val in = new BufferedReader(reader)) {
                in.readLine();

                if (conn instanceof final HttpURLConnection instance) {
                    val code = instance.getResponseCode();
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


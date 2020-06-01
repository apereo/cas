package org.apereo.cas.util.ssl;

import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.net.ssl.X509TrustManager;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link CompositeX509TrustManager}.
 * Represents an ordered list of {@link X509TrustManager}s with additive trust. If any one of the
 * composed managers trusts a certificate chain, then it is trusted by the composite manager.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class CompositeX509TrustManager implements X509TrustManager {
    private final List<X509TrustManager> trustManagers;

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        val trusted = this.trustManagers.stream().anyMatch(trustManager -> {
            try {
                trustManager.checkClientTrusted(chain, authType);
                return true;
            } catch (final CertificateException e) {
                val msg = "Unable to trust the client certificates [%s] for auth type [%s]: [%s]";
                LOGGER.debug(String.format(msg, Arrays.stream(chain)
                    .map(Certificate::toString).collect(Collectors.toSet()), authType, e.getMessage()), e);
                return false;
            }
        });

        if (!trusted) {
            throw new CertificateException("None of the TrustManagers can trust this client certificate chain");
        }
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {

        val trusted = this.trustManagers.stream().anyMatch(trustManager -> {
            try {
                trustManager.checkServerTrusted(chain, authType);
                return true;
            } catch (final CertificateException e) {
                val msg = "Unable to trust the server certificates [%s] for auth type [%s]: [%s]";
                LOGGER.debug(String.format(msg, Arrays.stream(chain)
                    .map(Certificate::toString).collect(Collectors.toSet()), authType, e.getMessage()), e);
                return false;
            }
        });
        if (!trusted) {
            throw new CertificateException("None of the TrustManagers trust this server certificate chain");
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        val certificates = new ArrayList<X509Certificate>(trustManagers.size());
        this.trustManagers.forEach(trustManager ->
            certificates.addAll(CollectionUtils.wrapList(trustManager.getAcceptedIssuers())));
        return certificates.toArray(X509Certificate[]::new);
    }

}

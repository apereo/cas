package org.apereo.cas.adaptors.x509.authentication;

import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * This class is adapted from the Tomcat SSLValve class and uses its parsing
 * logic.
 * Since this version is not a Valve running on every request, it has more debug
 * logging. It only handles the main cert that CAS uses and doesn't process all
 * the headers the SSLValve does.
 * When using mod_proxy_http, the client SSL information is not included in the
 * protocol (unlike mod_jk and mod_proxy_ajp). To make the client SSL
 * information available to Tomcat, some additional configuration is required.
 * In httpd, mod_headers is used to add the SSL information as HTTP headers. In
 * Tomcat, this valve is used to read the information from the HTTP headers and
 * insert it into the request.
 *
 * <b>Note: Ensure that the headers are always set by httpd for all requests to
 * prevent a client spoofing SSL information by sending fake headers. </b>
 * In httpd.conf add the following:
 * <p>
 * &lt;pre&gt;
 * &lt;IfModule ssl_module&gt;
 * RequestHeader set SSL_CLIENT_CERT "%{SSL_CLIENT_CERT}s"
 * &lt;/IfModule&gt;
 * &lt;/pre&gt;
 *
 * @author Apache Tomcat (copied and modified)
 * @author Hal Deadman
 * @since 5.3.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class RequestHeaderX509CertificateExtractor implements X509CertificateExtractor {

    /**
     * X509 Cert header.
     */
    public static final String X509_HEADER = "-----BEGIN CERTIFICATE-----";
    /**
     * X509 Cert footer.
     */
    public static final String X509_FOOTER = "-----END CERTIFICATE-----";

    private final String sslClientCertHeader;

    /**
     * Extract base64 encoded certificate from header and convert to {@link X509Certificate}.
     * Known behaviors of reverse proxies that are handled by the processing below:
     * - mod_header converts the '\n' into ' ' - nginx converts the '\n' into
     * multiple ' '
     * The code assumes that the trimmed header value starts with '-----BEGIN
     * CERTIFICATE-----' and ends with '-----END CERTIFICATE-----'.
     * Note: For Java 7, the the BEGIN and END markers must be on separate lines as
     * must each of the original content lines. The CertificateFactory is tolerant
     * of any additional whitespace such as leading and trailing spaces and new
     * lines as long as they do not appear in the middle of an original content
     * line.
     */
    @Override
    public X509Certificate[] extract(final HttpServletRequest request) {
        val certHeaderValue = getCertFromHeader(request);
        if (StringUtils.isBlank(certHeaderValue)) {
            LOGGER.debug("No header [{}] found in request (or value was null)", sslClientCertHeader);
            return null;
        }

        if (Objects.requireNonNull(certHeaderValue).length() < X509_HEADER.length()) {
            LOGGER.debug("Header [{}] found but it is too short to parse. Header value: [{}]", sslClientCertHeader, certHeaderValue);
            return null;
        }

        LOGGER.trace("Located value [{}] from header [{}]. Parsing...", certHeaderValue, sslClientCertHeader);
        val body = sanitizeCertificateBody(certHeaderValue);
        LOGGER.debug("Certificate body to parse is [{}]", body);
        
        try (val input = new ByteArrayInputStream(body.getBytes(StandardCharsets.ISO_8859_1))) {
            val cert = CertUtils.readCertificate(input);
            LOGGER.debug("Certificate extracted from header [{}] with subject: [{}]", sslClientCertHeader, cert.getSubjectDN());
            return new X509Certificate[]{cert};
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return null;
    }

    /**
     * Get certificate from header or return null.
     * HTTPD mod_header writes "(null)" when the ssl variable is not filled
     * so that is treated as if the header were not present or blank.
     *
     * @param request HTTP request object
     * @return Base64 encoded certificate
     */
    private String getCertFromHeader(final HttpServletRequest request) {
        val certHeaderValue = request.getHeader(sslClientCertHeader);
        if (StringUtils.isBlank(certHeaderValue)) {
            return null;
        }

        if ("(null)".equalsIgnoreCase(certHeaderValue)) {
            return null;
        }
        return StringUtils.trim(certHeaderValue);
    }

    private static String sanitizeCertificateBody(final String certHeaderValue) {
        var body = certHeaderValue.substring(X509_HEADER.length(), certHeaderValue.length() - X509_FOOTER.length());
        body = body.replace(' ', '\n');
        body = body.replace('\t', '\n');
        return X509_HEADER.concat("\n").concat(body).concat("\n").concat(X509_FOOTER).concat("\n");
    }
}

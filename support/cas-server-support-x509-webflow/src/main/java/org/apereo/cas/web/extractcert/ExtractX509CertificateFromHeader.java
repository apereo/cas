/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.cas.web.extractcert;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is adapted from the Tomcat SSLValve.java class and uses its
 * parsing logic.
 * 
 * Since this version is not a Valve running on every request, it has more debug
 * logging. It only handles the main cert that CAS uses and doesn't process all
 * the headers the SSLValve does.
 * 
 * When using mod_proxy_http, the client SSL information is not included in the
 * protocol (unlike mod_jk and mod_proxy_ajp). To make the client SSL
 * information available to Tomcat, some additional configuration is required.
 * In httpd, mod_headers is used to add the SSL information as HTTP headers. In
 * Tomcat, this valve is used to read the information from the HTTP headers and
 * insert it into the request.
 * <p>
 *
 * <b>Note: Ensure that the headers are always set by httpd for all requests to
 * prevent a client spoofing SSL information by sending fake headers. </b>
 * <p>
 *
 * In httpd.conf add the following:
 * 
 * <pre>
 * &lt;IfModule ssl_module&gt;
 *   RequestHeader set SSL_CLIENT_CERT "%{SSL_CLIENT_CERT}s"
 * &lt;/IfModule&gt;
 * </pre>
 * 
 * @author Apache Tomcat (copied and modified)
 * @author Hal Deadman
 * @since 5.3.0
 */
@Slf4j
public class ExtractX509CertificateFromHeader implements ExtractX509Certificate {

    private static final String X509_HEADER = "-----BEGIN CERTIFICATE-----";
    private static final String X509_FOOTER = "-----END CERTIFICATE-----";

    private String sslClientCertHeader = "ssl_client_cert";

    // ------------------------------------------------------ Constructor
    public ExtractX509CertificateFromHeader(final String sslClientCertHeader) {
        this.sslClientCertHeader = sslClientCertHeader;
    }

    public String getSslClientCertHeader() {
        return sslClientCertHeader;
    }

    private String mygetHeader(final HttpServletRequest request, final String header) {
        final String strcert0 = request.getHeader(header);
        if (strcert0 == null) {
            return null;
        }
        /* mod_header writes "(null)" when the ssl variable is not filled */
        if ("(null)".equals(strcert0)) {
            return null;
        }
        return strcert0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apereo.cas.adaptors.x509.authentication.certheader.ExtractSSLCertificate#
     * extract(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public X509Certificate[] extract(final HttpServletRequest request) {
        /*
         * Known behaviors of reverse proxies that are handled by the processing below:
         * - mod_header converts the '\n' into ' ' - nginx converts the '\n' into
         * multiple ' '
         *
         * The code assumes that the trimmed header value starts with '-----BEGIN
         * CERTIFICATE-----' and ends with '-----END CERTIFICATE-----'.
         *
         * Note: For Java 7, the the BEGIN and END markers must be on separate lines as
         * must each of the original content lines. The CertificateFactory is tolerant
         * of any additional whitespace such as leading and trailing spaces and new
         * lines as long as they do not appear in the middle of an original content
         * line.
         */
        String headerValue = mygetHeader(request, sslClientCertHeader);
        if (headerValue != null) {
            headerValue = headerValue.trim();
            if (headerValue.length() > X509_HEADER.length()) {
                String body = headerValue.substring(X509_HEADER.length(), headerValue.length() - X509_FOOTER.length());
                body = body.replace(' ', '\n');
                body = body.replace('\t', '\n');
                final String strcerts = X509_HEADER.concat("\n").concat(body).concat("\n").concat(X509_FOOTER).concat("\n");
                final ByteArrayInputStream bais = new ByteArrayInputStream(strcerts.getBytes(StandardCharsets.ISO_8859_1));
                X509Certificate[] jsseCerts = null;
                try {
                    final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    final X509Certificate cert = (X509Certificate) cf.generateCertificate(bais);
                    jsseCerts = new X509Certificate[1];
                    jsseCerts[0] = cert;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Certificate extracted from header {} with subject: {}", sslClientCertHeader,
                                cert.getSubjectDN());
                    }
                } catch (final java.security.cert.CertificateException e) {
                    LOGGER.warn("Error parsing the certificate in header: {} value: {} error msg: {}",
                            sslClientCertHeader, strcerts, e.getMessage());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Error parsing the certificate in header: {} value: {}", sslClientCertHeader,
                                strcerts, e);
                    }
                }
                return jsseCerts;
            } else {
                LOGGER.debug("Header [{}] found but it is too short to parse. Header value: {}", sslClientCertHeader,
                        headerValue);
            }
        } else {
            LOGGER.debug("No header [{}] found in request (or value was null)", sslClientCertHeader);
        }
        return null;
    }
}

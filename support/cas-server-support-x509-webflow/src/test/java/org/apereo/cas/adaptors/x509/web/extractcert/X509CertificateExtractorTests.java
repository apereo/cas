package org.apereo.cas.adaptors.x509.web.extractcert;

import org.apereo.cas.adaptors.x509.authentication.principal.AbstractX509CertificateTests;
import org.apereo.cas.web.extractcert.RequestHeaderX509CertificateExtractor;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Hal Deadman
 * @since 5.3.0
 */
public class X509CertificateExtractorTests extends AbstractX509CertificateTests {

    private static final String[] CERTIFICATE_LINES = new String[]{
        "-----BEGIN CERTIFICATE-----",
        "MIIFXTCCA0WgAwIBAgIJANFf3YTJgYifMA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNV",
        "BAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBX",
        "aWRnaXRzIFB0eSBMdGQwHhcNMTcwNTI2MjEzNjM3WhcNMTgwNTI2MjEzNjM3WjBF",
        "MQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50",
        "ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIIC",
        "CgKCAgEA2ykNBanZz4cVITNpZcWNVmErUzqgSNrK361mj9vEdB1UkHatwal9jVrR",
        "QvFgfiZ8Gl+/85t0ebJhJ+rIr1ww6JE7v2s2MThENj95K5EwZOmgvw+CBlBYsFIz",
        "8BtjlVYy+v7RaGPXfjrFkexQP9UIaiIIog2ClDZirRvb+QxS930/YW5Qo+X6EX6W",
        "/m/HvlorD25U4ni2FQ0y+EMO2e1jD88cAAMoP5f+Mf6NBK8I6yUeaSuMq7WqtHGV",
        "e4F1WOg5z9J5c/M69rB0iQr5NUQwZ1mPYf5Kr0P6+TLh8DJphbVvmHJyT3bgofeV",
        "JYl/kdjiXS5P/jwY9tfmhu04tsyzopWRUFCcj5zCiqZYaMn0wtDn08KaAh9oOlg8",
        "Z6mJ9i5EybkLm63W7z7LxuM+qnYzq4wKkKdx8hbpASwPqzJkJeXFL/LzhKdZuHiR",
        "clgPVYnm98URwhObh073dKguG/gkhcnpXcVBBVdVTJZYGBvTpQh0afXd9bcBwOzY",
        "t4MDpGiQB2fLzBOEZhQ37kUcWPmZw5bNPxhx4yE96Md0rx/Gu4ipAHuqLemb1SL5",
        "uWNesVmgY3OXaIamQIm9BCwkf8mMvoYdAT+lukTUZLtJ6s2w+Oxnl10tmb+6sTXy",
        "UB3WcBTp/o3YjAyJPnM1Wq6nVNQ4W2+NbV5purGAP09sumxeJj8CAwEAAaNQME4w",
        "HQYDVR0OBBYEFCGOYMvymUG2ZZT+lK4LvwEvx731MB8GA1UdIwQYMBaAFCGOYMvy",
        "mUG2ZZT+lK4LvwEvx731MAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggIB",
        "AG6m4nDYCompUtRVude1qulwwAaYEMHyIIsfymI8uAE7d2o4bGjVpAUOdH/VWSOp",
        "Rzx0oK6K9cHyiBlKHw5zSZdqcRi++tDX3P9Iy5tXO//zkhMEnSpk6RF2+9JXtyhx",
        "Gma4yAET1yES+ybiFT21uZrGCC9r69rWG8JRZshc4RVWGwZsd0zrATVqfY0mZurm",
        "xLgU4UOvkTczjlrLiklwwU68M1DLcILJ5FZGTWeTJ/q1wpIn9isK2siAW/VOcbuG",
        "xdbGladnIFv+iQfuZG0yjcuMsBFsQiXi6ONM8GM+dr+61V63/1s73jYcOToEsTMM",
        "3bHeVffoSkhZvOGTRCI6QhK9wqnIKhAYqu+NbV4OphfE3gOaK+T1cASXUtSQPXoa",
        "sEoIVmbQsWRBhWvYShVqvINsH/hAT3Cf/+SslprtQUqiyt2ljdgrRFZdoyB3S7ky",
        "KWoZRvHRj2cKU65LVYwx6U1A8SGmViz4aHMSai0wwKzOVv9MGHeRaVhlMmMsbdfu",
        "wKoKJv0xYoVwEh1rB8TH8PjbL+6eFLeZXYVZnH71d5JHCghZ8W0a11ZuYkscSQWk",
        "yoTBqEpJloWksrypqp3iL4PAL5+KkB2zp66+MVAg8LcEDFJggBBJCtv4SCWV7ZOB",
        "WLu8gep+XCwSn0Wb6D3eFs4DoIiMvQ6g2rS/pk7o5eWj",
        "-----END CERTIFICATE-----"};

    private final RequestHeaderX509CertificateExtractor extractX509CertificateFromHeader
        = new RequestHeaderX509CertificateExtractor("ssl_client_cert");

    private static String certificateSingleLine(final String[] lines, final String separator) {
        val singleSpaced = new StringBuilder();
        for (val current : lines) {
            singleSpaced.append(current).append(separator);
        }
        singleSpaced.deleteCharAt(singleSpaced.length() - 1);
        return singleSpaced.toString();
    }

    private static String certificateSingleLine(final String separator) {
        return certificateSingleLine(CERTIFICATE_LINES, separator);
    }

    private static void assertCertificateParsed(final X509Certificate[] certificates) {
        assertNotNull(certificates);
        assertEquals(1, certificates.length);
        assertNotNull(certificates[0]);
    }

    @Test
    public void verifyExtractX509FromHeaderSpaceSeperator() {
        val request = new MockHttpServletRequest();
        request.addHeader(extractX509CertificateFromHeader.getSslClientCertHeader(), certificateSingleLine(" "));
        assertCertificateParsed(extractX509CertificateFromHeader.extract(request));
    }

    @Test
    public void verifyExtractX509FromHeaderNoSeparator() {
        val request = new MockHttpServletRequest();
        request.addHeader(extractX509CertificateFromHeader.getSslClientCertHeader(), certificateSingleLine("\t"));
        assertCertificateParsed(extractX509CertificateFromHeader.extract(request));
    }

    @Test
    public void verifyExtractX509FromHeaderNoHeader() {
        val request = new MockHttpServletRequest();
        assertNull(extractX509CertificateFromHeader.extract(request));
    }
}

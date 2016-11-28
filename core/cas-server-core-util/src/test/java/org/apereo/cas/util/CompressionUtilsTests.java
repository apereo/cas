package org.apereo.cas.util;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * This is {@link CompressionUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CompressionUtilsTests {

    @Test
    public void verifyStringCompression() throws Exception {
        final String srcTxt =
                "lamEiLCJhZG1pbiI6dHJ1ZX0.|..03f329983b86f7d9a9f5fef85305880101d5e302afafa20154d094b229f757|eyJhbGciO"
                        + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
                        + "xlamEiLCJhZG1pbiI6dHJ1ZX0.03f329983b86f7d9a9f5fef85305880101d5e302afafa20154d094b229f757";

        final String str = CompressionUtils.compress(srcTxt);
        StringWriter fw = new StringWriter();
        IOUtils.write(str, fw);
        IOUtils.closeQuietly(fw);
        final String v = new String(Charset.forName("UTF-8").encode(fw.toString()).array(), Charset.forName("UTF-8"));
        final String originalStr = CompressionUtils.decompress(v);
        fw = new StringWriter();
        IOUtils.write(originalStr, fw);
    }
}

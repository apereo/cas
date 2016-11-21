package org.apereo.cas.ticket.registry.jwt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This is {@link Test}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class Test {
    public static void main(String[] args) throws Exception {
        String string = "W1GyVfgWuGX5Q-VJsTjk_kvPmQYIcQYsArD0MEHeb0gaZD1Mz1gTI_23waMi5h0ngLtT924FckHw4Sr0lKb41Q";
        System.out.println(string.length());

        System.out.println("after compress:");
        byte[] compressed = compress(string);

        System.out.println(compressed.toString());
        System.out.println("after decompress:");
        String decomp = decompress(compressed);
        System.out.println(decomp);
    }


    public static byte[] compress(String str) throws Exception {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return obj.toByteArray();
    }

    public static String decompress(byte[] bytes) throws Exception {
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        String outStr = "";
        String line;
        while ((line = bf.readLine()) != null) {
            outStr += line;
        }
        return outStr;
    }
}

package org.jasig.cas.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by ruaa on 2015. 11. 4..
 */
public class ParamBodyWriteCallback implements HttpTemplate.BodyWriteCallback<Map<String, String>> {
    @Override
    public void write(HttpURLConnection conn, Map<String, String> body) {
        try {
            StringBuilder sbBody = new StringBuilder();

            for (String key : body.keySet()) {
                sbBody.append(String.format("%s=%s&", key, body.get(key)));
            }
            sbBody.deleteCharAt(sbBody.length() - 1);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(sbBody.toString());
            wr.flush();
            wr.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

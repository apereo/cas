package org.jasig.cas.web.flow;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * This is {@link ClientPreemptiveDigestAuthentication}.
 *
 * @author Misagh Moayyed
 * @since x.y.z
 */
public class ClientPreemptiveDigestAuthentication {

    public static void main(String[] args) throws Exception {
        final HttpHost target = new HttpHost("httpbin.org", 80, "http");
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials("user", "passwd"));
        final CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        try {

            // Create AuthCache instance
            final AuthCache authCache = new BasicAuthCache();
            // Generate DIGEST scheme object, initialize it and add it to the local
            // auth cache
            final DigestScheme digestAuth = new DigestScheme();
            // Suppose we already know the realm name
            digestAuth.overrideParamter("realm", "some realm");
            // Suppose we already know the expected nonce value
            digestAuth.overrideParamter("nonce", "whatever");
            authCache.put(target, digestAuth);

            // Add AuthCache to the execution context
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            final HttpGet httpget = new HttpGet("http://httpbin.org/digest-auth/auth/user/passwd");

            System.out.println("Executing request " + httpget.getRequestLine() + " to target " + target);
            for (int i = 0; i < 3; i++) {
                final CloseableHttpResponse response = httpclient.execute(target, httpget, localContext);
                try {
                    System.out.println("----------------------------------------");
                    System.out.println(response.getStatusLine());
                    System.out.println(EntityUtils.toString(response.getEntity()));
                } finally {
                    response.close();
                }
            }
        } finally {
            httpclient.close();
        }
    }

}

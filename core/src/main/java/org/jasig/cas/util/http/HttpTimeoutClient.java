package org.jasig.cas.util.http;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import sun.net.www.http.HttpClient;

/**
 * Based on code from:
 * http://coding.derkeiler.com/Archive/Java/comp.lang.java.programmer/2004-01/3271.html
 */
public class HttpTimeoutClient extends HttpClient {

    private static int iSoTimeout = 0;

    public HttpTimeoutClient(URL url, String proxy, int proxyPort)
        throws IOException {
        super(url, proxy, proxyPort);
    }

    public HttpTimeoutClient(URL url) throws IOException {
        super(url, null, -1);
    }

    public static HttpTimeoutClient getNew(URL url) throws IOException {
        HttpTimeoutClient httpTimeoutClient = (HttpTimeoutClient) kac.get(url);

        if (httpTimeoutClient == null) {
            httpTimeoutClient = new HttpTimeoutClient(url); // CTOR called openServer()
        } else {
            httpTimeoutClient.url = url;
        }

        return httpTimeoutClient;
    }

    public static void setSoTimeout(int iNewSoTimeout) {
        iSoTimeout = iNewSoTimeout;
    }

    public static int getSoTimeout() {
        return iSoTimeout;
    }

    // Override doConnect in NetworkClient

    protected Socket doConnect(String s, int i) throws IOException,
        UnknownHostException, SocketException {
        Socket socket = super.doConnect(s, i);

        // This is the important bit
        socket.setSoTimeout(iSoTimeout);
        return socket;
    }
}
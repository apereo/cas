/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class CallableMessageSender implements Callable<Boolean> {

    private String url;

    private String message;

    private int readTimeout;

    private int connectionTimeout;

    private boolean followRedirects;
    
    private String contentType = "application/x-www-form-urlencoded";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CallableMessageSender.class);
   
    /**
     * Prepare the sender with a given url and the message to send.
     * @param url the url to which the message will be sent.
     * @param message the message itself.
     */
    public CallableMessageSender(final String url, final String message) {
        this.url = url;
        this.message = message;
    }

    public final void setContentType(final String type) {
        this.contentType = type;
    }
   
    public final void setReadTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public final void setConnectionTimeout(final int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public final void setFollowRedirects(final boolean followRedirects) {
        this.followRedirects = followRedirects;
    }
    
    @Override
    public final Boolean call() throws Exception {
        HttpURLConnection connection = null;
        BufferedReader in = null;
        try {
            LOGGER.debug("Attempting to access {}", url);
            final URL logoutUrl = new URL(url);
            final String output = formatOutputMessageInternal(this.message);

            connection = (HttpURLConnection) logoutUrl.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setReadTimeout(this.readTimeout);
            connection.setConnectTimeout(this.connectionTimeout);
            connection.setInstanceFollowRedirects(this.followRedirects);
            connection.setRequestProperty("Content-Length", Integer.toString(output.getBytes().length));
            connection.setRequestProperty("Content-Type", this.contentType);
            final DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
            printout.writeBytes(output);
            printout.flush();
            printout.close();

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            boolean readInput = true;
            while (readInput) {
                readInput = StringUtils.isNotBlank(in.readLine());
            }

            LOGGER.debug("Finished sending message to {}", url);
            return true;
        } catch (final SocketTimeoutException e) {
            LOGGER.warn("Socket Timeout Detected while attempting to send message to [{}]", url);
            return false;
        } catch (final Exception e) {
            LOGGER.warn("Error Sending message to url endpoint [{}]. Error is [{}]", url, e.getMessage());
            return false;
        } finally {
            IOUtils.closeQuietly(in);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Encodes the message in UTF-8 format in preparation to send.
     * @param message Message to format and encode
     * @return The encoded message.
     */
    protected String formatOutputMessageInternal(final String message) {
        try {
            return URLEncoder.encode(message, "UTF-8"); 
        } catch (final UnsupportedEncodingException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return message;
    }

}


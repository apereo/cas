/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.util.http;

import javax.validation.constraints.NotNull;
import java.net.URL;

/**
 * Define the behaviour of a HTTP client.
 *
 * @author Jerome Leleu
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public interface HttpClient {

    /**
     * Sends a message to a particular endpoint.  Option of sending it without
     * waiting to ensure a response was returned.
     * <p>
     * This is useful when it doesn't matter about the response as you'll perform no action based on the response.
     *
     * @param message The message that should be sent to the http endpoint
     * @return boolean if the message was sent, or async was used.  false if the message failed.
     * @since 4.1.0
     */
    boolean sendMessageToEndPoint(@NotNull HttpMessage message);

    /**
     * Make a synchronous HTTP(S) call to ensure that the url is reachable.
     *
     * @param url the url to call
     * @return whether the url is valid
     */
    boolean isValidEndPoint(@NotNull String url);

    /**
     * Make a synchronous HTTP(S) call to ensure that the url is reachable.
     *
     * @param url the url to call
     * @return whether the url is valid
     */
    boolean isValidEndPoint(@NotNull URL url);
}

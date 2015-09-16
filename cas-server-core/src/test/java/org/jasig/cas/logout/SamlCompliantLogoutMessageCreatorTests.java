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
package org.jasig.cas.logout;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.SingleLogoutService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(JUnit4.class)
public class SamlCompliantLogoutMessageCreatorTests {

    private final LogoutMessageCreator builder = new SamlCompliantLogoutMessageCreator();

    @Test
    public void verifyMessageBuilding() throws Exception {

        final SingleLogoutService service = mock(SingleLogoutService.class);
        when(service.getOriginalUrl()).thenReturn(TestUtils.CONST_TEST_URL);
        final URL logoutUrl = new URL(service.getOriginalUrl());
        final DefaultLogoutRequest request = new DefaultLogoutRequest("TICKET-ID", service, logoutUrl);

        final String msg = builder.create(request);

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final InputStream is = new ByteArrayInputStream(msg.getBytes());
        final Document document = builder.parse(is);
        
        final NodeList list = document.getDocumentElement().getElementsByTagName("samlp:SessionIndex");
        assertEquals(list.getLength(), 1);
        
        assertEquals(list.item(0).getTextContent(), request.getTicketId());
    }
}

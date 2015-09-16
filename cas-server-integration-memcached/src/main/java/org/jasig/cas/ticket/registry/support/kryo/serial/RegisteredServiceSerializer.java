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
package org.jasig.cas.ticket.registry.support.kryo.serial;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializier for {@link org.jasig.cas.services.RegisteredService} instances.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RegisteredServiceSerializer  extends Serializer<RegisteredService> {
    
    @Override
    public void write(final Kryo kryo, final Output output, final RegisteredService service) {
        kryo.writeObject(output, service.getServiceId());
    }

    @Override
    public RegisteredService read(final Kryo kryo, final Input input, final Class<RegisteredService> type) {
        final String id = kryo.readObject(input, String.class);
        final AbstractRegisteredService svc = new RegisteredServiceImpl();
        svc.setServiceId(id);
        return svc;
    }
}

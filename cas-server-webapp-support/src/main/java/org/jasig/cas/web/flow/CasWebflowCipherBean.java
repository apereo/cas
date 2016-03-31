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
package org.jasig.cas.web.flow;

import org.cryptacular.bean.CipherBean;
import org.jasig.cas.util.CipherExecutor;

import javax.naming.OperationNotSupportedException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is {@link CasWebflowCipherBean}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasWebflowCipherBean implements CipherBean {
    private final CipherExecutor<byte[], byte[]> webflowCipherExecutor;

    /**
     * Instantiates a new Cas webflow cipher bean.
     *
     * @param cipherExecutor the cipher executor
     */
    public CasWebflowCipherBean(final CipherExecutor cipherExecutor) {
        this.webflowCipherExecutor = cipherExecutor;
    }

    @Override
    public byte[] encrypt(final byte[] bytes) {
        return webflowCipherExecutor.encode(bytes);
    }

    @Override
    public void encrypt(final InputStream inputStream, final OutputStream outputStream) {
        throw new RuntimeException(new OperationNotSupportedException("Encrypting input stream is not supported"));
    }

    @Override
    public byte[] decrypt(final byte[] bytes) {
        return webflowCipherExecutor.decode(bytes);
    }

    @Override
    public void decrypt(final InputStream inputStream, final OutputStream outputStream) {
        throw new RuntimeException(new OperationNotSupportedException("Decrypting input stream is not supported"));
    }
}

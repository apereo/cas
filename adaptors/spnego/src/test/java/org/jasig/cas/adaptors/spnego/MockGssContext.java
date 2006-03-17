/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego;

import java.io.InputStream;
import java.io.OutputStream;

import org.ietf.jgss.ChannelBinding;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;


public class MockGssContext implements GSSContext {

    private final boolean established;
    
    public MockGssContext(final boolean established) {
        this.established = established;
    }
    
    public byte[] initSecContext(byte[] arg0, int arg1, int arg2)
        throws GSSException {
        return null;
    }

    public int initSecContext(InputStream arg0, OutputStream arg1)
        throws GSSException {
        return 0;
    }

    public byte[] acceptSecContext(byte[] arg0, int arg1, int arg2)
        throws GSSException {
        return null;
    }

    public void acceptSecContext(InputStream arg0, OutputStream arg1)
        throws GSSException {
        // nothing to do
    }

    public boolean isEstablished() {
        return this.established;
    }

    public void dispose() throws GSSException {
        // nothing to do
    }

    public int getWrapSizeLimit(int arg0, boolean arg1, int arg2)
        throws GSSException {
        return 0;
    }

    public byte[] wrap(byte[] arg0, int arg1, int arg2, MessageProp arg3)
        throws GSSException {
        return null;
    }

    public void wrap(InputStream arg0, OutputStream arg1, MessageProp arg2)
        throws GSSException {
        // nothing to do
    }

    public byte[] unwrap(byte[] arg0, int arg1, int arg2, MessageProp arg3)
        throws GSSException {
        return null;
    }

    public void unwrap(InputStream arg0, OutputStream arg1, MessageProp arg2)
        throws GSSException {
        // nothing to do
    }

    public byte[] getMIC(byte[] arg0, int arg1, int arg2, MessageProp arg3)
        throws GSSException {
        return null;
    }

    public void getMIC(InputStream arg0, OutputStream arg1, MessageProp arg2)
        throws GSSException {
        // nothing to do
    }

    public void verifyMIC(byte[] arg0, int arg1, int arg2, byte[] arg3,
        int arg4, int arg5, MessageProp arg6) throws GSSException {
        // nothing to do
    }

    public void verifyMIC(InputStream arg0, InputStream arg1, MessageProp arg2)
        throws GSSException {
        // nothing to do
    }

    public byte[] export() throws GSSException {
        return null;
    }

    public void requestMutualAuth(boolean arg0) throws GSSException {
        // nothing to do
    }

    public void requestReplayDet(boolean arg0) throws GSSException {
        // nothing to do
    }

    public void requestSequenceDet(boolean arg0) throws GSSException {
        // nothing to do
    }

    public void requestCredDeleg(boolean arg0) throws GSSException {
        // nothing to do
}

    public void requestAnonymity(boolean arg0) throws GSSException {
        // nothing to do
    }

    public void requestConf(boolean arg0) throws GSSException {
        // nothing to do
    }

    public void requestInteg(boolean arg0) throws GSSException {
        // nothing to do
    }

    public void requestLifetime(int arg0) throws GSSException {
        // nothing to do
    }

    public void setChannelBinding(ChannelBinding arg0) throws GSSException {
        // nothing to do
    }

    public boolean getCredDelegState() {
        return false;
    }

    public boolean getMutualAuthState() {
        return false;
    }

    public boolean getReplayDetState() {
        return false;
    }

    public boolean getSequenceDetState() {
        return false;
    }

    public boolean getAnonymityState() {
        return false;
    }

    public boolean isTransferable() throws GSSException {
        return false;
    }

    public boolean isProtReady() {
        return false;
    }

    public boolean getConfState() {
        return false;
    }

    public boolean getIntegState() {
        return false;
    }

    public int getLifetime() {
        return 0;
    }

    public GSSName getSrcName() throws GSSException {
        return new GSSName() {

            public GSSName canonicalize(Oid arg0) throws GSSException {
                return null;
            }

            public boolean equals(GSSName arg0) throws GSSException {
                return false;
            }

            public byte[] export() throws GSSException {
                return null;
            }

            public Oid getStringNameType() throws GSSException {
                return null;
            }

            public boolean isAnonymous() {
                return false;
            }

            public boolean isMN() {
                return false;
            }

            public String toString() {
                return "test";
            }
        };
    }

    public GSSName getTargName() throws GSSException {
        return null;
    }

    public Oid getMech() throws GSSException {
        return null;
    }

    public GSSCredential getDelegCred() throws GSSException {
        return null;
    }

    public boolean isInitiator() throws GSSException {
        return false;
    }

}

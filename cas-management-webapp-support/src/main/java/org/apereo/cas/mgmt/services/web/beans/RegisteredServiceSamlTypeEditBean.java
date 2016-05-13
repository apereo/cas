package org.apereo.cas.mgmt.services.web.beans;

/**
 * This is {@link RegisteredServiceSamlTypeEditBean}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RegisteredServiceSamlTypeEditBean {
    private boolean signResp;
    private boolean signAssert;
    private boolean encAssert;
    private String authCtxCls;
    private String mdLoc;
    private long mdMaxVal;
    private String mdSigLoc;

    public boolean isSignResp() {
        return signResp;
    }

    public void setSignResp(final boolean signResp) {
        this.signResp = signResp;
    }

    public boolean isSignAssert() {
        return signAssert;
    }

    public void setSignAssert(final boolean signAssert) {
        this.signAssert = signAssert;
    }

    public boolean isEncAssert() {
        return encAssert;
    }

    public void setEncAssert(final boolean encAssert) {
        this.encAssert = encAssert;
    }

    public String getAuthCtxCls() {
        return authCtxCls;
    }

    public void setAuthCtxCls(final String authCtxCls) {
        this.authCtxCls = authCtxCls;
    }

    public String getMdLoc() {
        return mdLoc;
    }

    public void setMdLoc(final String mdLoc) {
        this.mdLoc = mdLoc;
    }

    public long getMdMaxVal() {
        return mdMaxVal;
    }

    public void setMdMaxVal(final long mdMaxVal) {
        this.mdMaxVal = mdMaxVal;
    }

    public String getMdSigLoc() {
        return mdSigLoc;
    }

    public void setMdSigLoc(final String mdSigLoc) {
        this.mdSigLoc = mdSigLoc;
    }
}

package org.apereo.cas.mgmt.services.web.beans;

import java.util.Set;

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
    private boolean removeEmptyEntities;
    private boolean removeRoleless;
    private String mdPattern;
    private String dir;
    private Set<String> roles;
    private String rqrdNmeIdFmt;
    private String svcPrvdrNmeQlfr;
    private String nmeQlfr;

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

    public boolean isRemoveEmptyEntities() {
        return removeEmptyEntities;
    }

    public void setRemoveEmptyEntities(final boolean removeEmptyEntities) {
        this.removeEmptyEntities = removeEmptyEntities;
    }

    public boolean isRemoveRoleless() {
        return removeRoleless;
    }

    public void setRemoveRoleless(final boolean removeRoleless) {
        this.removeRoleless = removeRoleless;
    }

    public String getMdPattern() {
        return mdPattern;
    }

    public void setMdPattern(final String mdPattern) {
        this.mdPattern = mdPattern;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(final String dir) {
        this.dir = dir;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(final Set<String> roles) {
        this.roles = roles;
    }
    
    public String getRqrdNmeIdFmt() {
        return rqrdNmeIdFmt;
    }

    public void setRqrdNmeIdFmt(String rdNmeIdFmt) {
        this.rqrdNmeIdFmt = rdNmeIdFmt;
    }

    public String getSvcPrvdrNmeQlfr() {
        return svcPrvdrNmeQlfr;
    }

    public void setSvcPrvdrNmeQlfr(String svcPrvdrNmeQlfr) {
        this.svcPrvdrNmeQlfr = svcPrvdrNmeQlfr;
    }

    public String getNmeQlfr() {
        return nmeQlfr;
    }

    public void setNmeQlfr(String nmeQlfr) {
        this.nmeQlfr = nmeQlfr;
    }
}

package org.jasig.cas.web.support.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * This is {@link AuditTrailEntity} that represents the audit table.
 * Schema is generated automatically.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Entity(name="COM_AUDIT_TRAIL")
public class AuditTrailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="AUD_USER")
    private String user;

    @Column(name="AUD_CLIENT_IP")
    private String clientIp;

    @Column(name="AUD_SERVER_IP")
    private String serverIp;

    @Column(name="AUD_RESOURCE")
    private String resource;

    @Column(name="AUD_ACTION")
    private String action;

    @Column(name="APPLIC_CD")
    private String applicationCode;

    @Column(name="AUD_DATE", nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(final String clientIp) {
        this.clientIp = clientIp;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(final String serverIp) {
        this.serverIp = serverIp;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(final String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(final String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }
}

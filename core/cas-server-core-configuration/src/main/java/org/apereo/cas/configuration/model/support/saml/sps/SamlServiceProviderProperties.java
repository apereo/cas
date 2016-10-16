package org.apereo.cas.configuration.model.support.saml.sps;

import com.google.common.collect.Lists;

/**
 * This is {@link SamlServiceProviderProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlServiceProviderProperties {
    private Dropbox dropbox = new Dropbox();
    private Workday workday = new Workday();
    private SAManage saManage = new SAManage();
    private Salesforce salesforce = new Salesforce();
    private ServiceNow serviceNow = new ServiceNow();
    private Box box = new Box();
    private NetPartner netPartner = new NetPartner();
    private Webex webex = new Webex();
    private Office365 office365 = new Office365();
    private TestShib testShib = new TestShib();

    public TestShib getTestShib() {
        return testShib;
    }

    public void setTestShib(final TestShib testShib) {
        this.testShib = testShib;
    }

    public Office365 getOffice365() {
        return office365;
    }

    public void setOffice365(final Office365 office365) {
        this.office365 = office365;
    }

    public Webex getWebex() {
        return webex;
    }

    public void setWebex(final Webex webex) {
        this.webex = webex;
    }

    public NetPartner getNetPartner() {
        return netPartner;
    }

    public void setNetPartner(final NetPartner netPartner) {
        this.netPartner = netPartner;
    }

    public ServiceNow getServiceNow() {
        return serviceNow;
    }

    public void setServiceNow(final ServiceNow serviceNow) {
        this.serviceNow = serviceNow;
    }

    public Box getBox() {
        return box;
    }

    public void setBox(final Box box) {
        this.box = box;
    }

    public Salesforce getSalesforce() {
        return salesforce;
    }

    public void setSalesforce(final Salesforce salesforce) {
        this.salesforce = salesforce;
    }

    public SAManage getSaManage() {
        return saManage;
    }

    public void setSaManage(final SAManage saManage) {
        this.saManage = saManage;
    }

    public Workday getWorkday() {
        return workday;
    }

    public void setWorkday(final Workday workday) {
        this.workday = workday;
    }

    public Dropbox getDropbox() {
        return dropbox;
    }

    public void setDropbox(final Dropbox dropbox) {
        this.dropbox = dropbox;
    }

    public static class Dropbox extends AbstractSamlSPProperties {
        public Dropbox() {
            setNameIdAttribute("mail");
        }
    }

    public static class Box extends AbstractSamlSPProperties {
        public Box() {
            setAttributes(Lists.newArrayList("email", "firstName", "lastName"));
        }
    }

    public static class SAManage extends AbstractSamlSPProperties {
        public SAManage() {
            setNameIdAttribute("mail");
        }
    }

    public static class Workday extends AbstractSamlSPProperties {
    }

    public static class Salesforce extends AbstractSamlSPProperties {
        public Salesforce() {
            setAttributes(Lists.newArrayList("mail", "eduPersonPrincipalName"));
        }
    }

    public static class ServiceNow extends AbstractSamlSPProperties {
        public ServiceNow() {
            setAttributes(Lists.newArrayList("eduPersonPrincipalName"));
        }
    }

    public static class NetPartner extends AbstractSamlSPProperties {
        public NetPartner() {
            setNameIdAttribute("studentId");
        }
    }
    
    public static class Office365 extends AbstractSamlSPProperties {
        public Office365() {
            setNameIdAttribute("scopedImmutableID");
            setAttributes(Lists.newArrayList("IDPEmail,ImmutableID"));
        }
    }
    public static class Webex extends AbstractSamlSPProperties {
        public Webex() {
            setNameIdAttribute("email");
            setAttributes(Lists.newArrayList("firstName,lastName"));
        }
    }
    public static class TestShib extends AbstractSamlSPProperties {
        public TestShib() {
            setMetadata("http://www.testshib.org/metadata/testshib-providers.xml");
            setAttributes(Lists.newArrayList("eduPersonPrincipalName"));
        }
    }
}

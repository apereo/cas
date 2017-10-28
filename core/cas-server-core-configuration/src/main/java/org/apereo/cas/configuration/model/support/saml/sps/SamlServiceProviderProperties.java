package org.apereo.cas.configuration.model.support.saml.sps;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.CollectionUtils;

import java.io.Serializable;

/**
 * This is {@link SamlServiceProviderProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml-sp-integrations")
public class SamlServiceProviderProperties implements Serializable {
    /**
     * {@code email} attribute to release.
     */
    private static final String EMAIL = "email";
    /**
     * {@code eduPersonPrincipalName} attribute to release.
     */
    private static final String PRINCIPAL_NAME = "eduPersonPrincipalName";
    private static final long serialVersionUID = 8602328179113963081L;

    /**
     * Settings related to Dropbox acting as a SAML service provider.
     */
    private Dropbox dropbox = new Dropbox();
    /**
     * Settings related to Workday acting as a SAML service provider.
     */
    private Workday workday = new Workday();
    /**
     * Settings related to SA Manage acting as a SAML service provider.
     */
    private SAManage saManage = new SAManage();
    /**
     * Settings related to Salesforce acting as a SAML service provider.
     */
    private Salesforce salesforce = new Salesforce();
    /**
     * Settings related to ServiceNow acting as a SAML service provider.
     */
    private ServiceNow serviceNow = new ServiceNow();
    /**
     * Settings related to Box acting as a SAML service provider.
     */
    private Box box = new Box();
    /**
     * Settings related to NetPartner acting as a SAML service provider.
     */
    private NetPartner netPartner = new NetPartner();
    /**
     * Settings related to Webex acting as a SAML service provider.
     */
    private Webex webex = new Webex();
    /**
     * Settings related to Office365 acting as a SAML service provider.
     */
    private Office365 office365 = new Office365();
    /**
     * Settings related to TestShib acting as a SAML service provider.
     */
    private TestShib testShib = new TestShib();
    /**
     * Settings related to InCommon acting as a SAML service provider.
     */
    private InCommon inCommon = new InCommon();
    /**
     * Settings related to ZOOM acting as a SAML service provider.
     */
    private Zoom zoom = new Zoom();
    /**
     * Settings related to Evernote acting as a SAML service provider.
     */
    private Evernote evernote = new Evernote();
    /**
     * Settings related to Asana acting as a SAML service provider.
     */
    private Asana asana = new Asana();
    /**
     * Settings related to Gartner acting as a SAML service provider.
     */
    private Gartner gartner = new Gartner();
    /**
     * Settings related to Tableu acting as a SAML service provider.
     */
    private Tableau tableau = new Tableau();
    /**
     * Settings related to WebAdvisor acting as a SAML service provider.
     */
    private WebAdvisor webAdvisor = new WebAdvisor();
    /**
     * Settings related to OpenAthens acting as a SAML service provider.
     */
    private OpenAthens openAthens = new OpenAthens();
    /**
     * Settings related to ArcGIS acting as a SAML service provider.
     */
    private ArcGIS arcGIS = new ArcGIS();
    /**
     * Settings related to BenefitFocus acting as a SAML service provider.
     */
    private BenefitFocus benefitFocus = new BenefitFocus();
    /**
     * Settings related to Adobe Cloud acting as a SAML service provider.
     */
    private AdobeCloud adobeCloud = new AdobeCloud();
    /**
     * Settings related to Academic Works acting as a SAML service provider.
     */
    private AcademicWorks academicWorks = new AcademicWorks();
    /**
     * Settings related to Easy IEP acting as a SAML service provider.
     */
    private EasyIep easyIep = new EasyIep();
    /**
     * Settings related to InfiniteCampus acting as a SAML service provider.
     */
    private InfiniteCampus infiniteCampus = new InfiniteCampus();
    /**
     * Settings related to SecuringTheHuman acting as a SAML service provider.
     */
    private SecuringTheHuman sansSth = new SecuringTheHuman();
    /**
     * Settings related to Slack acting as a SAML service provider.
     */
    private Slack slack = new Slack();
    /**
     * Settings related to Zendesk acting as a SAML service provider.
     */
    private Zendesk zendesk = new Zendesk();
    /**
     * Settings related to Bynder acting as a SAML service provider.
     */
    private Bynder bynder = new Bynder();
    /**
     * Settings related to Famis acting as a SAML service provider.
     */
    private Famis famis = new Famis();
    /**
     * Settings related to Sunshine state ed/release alliance acting as a SAML service provider.
     */
    private SunshineStateEdResearchAlliance sserca = new SunshineStateEdResearchAlliance();

    /**
     * Settings related to EverBridge acting as a SAML service provider.
     */
    private EverBridge everBridge = new EverBridge();
    /**
     * Settings related to CherWell acting as a SAML service provider.
     */
    private CherWell cherWell = new CherWell();
    /**
     * Settings related to CherWell acting as a SAML service provider.
     */
    private Egnyte egnyte = new Egnyte();
    /**
     * Settings related to CherWell acting as a SAML service provider.
     */
    private NewRelic newRelic = new NewRelic();
    /**
     * Settings related to Yuja acting as a SAML service provider.
     */
    private Yuja yuja = new Yuja();
    /**
     * Settings related to Symplicity acting as a SAML service provider.
     */
    private Symplicity symplicity = new Symplicity();

    public Famis getFamis() {
        return famis;
    }

    public void setFamis(final Famis famis) {
        this.famis = famis;
    }

    public Egnyte getEgnyte() {
        return egnyte;
    }

    public Yuja getYuja() {
        return yuja;
    }

    public void setYuja(final Yuja yuja) {
        this.yuja = yuja;
    }

    public Symplicity getSymplicity() {
        return symplicity;
    }

    public void setSymplicity(final Symplicity symplicity) {
        this.symplicity = symplicity;
    }

    public void setEgnyte(final Egnyte egnyte) {
        this.egnyte = egnyte;
    }

    public NewRelic getNewRelic() {
        return newRelic;
    }

    public void setNewRelic(final NewRelic newRelic) {
        this.newRelic = newRelic;
    }

    public SunshineStateEdResearchAlliance getSserca() {
        return sserca;
    }

    public void setSserca(final SunshineStateEdResearchAlliance sserca) {
        this.sserca = sserca;
    }

    public CherWell getCherWell() {
        return cherWell;
    }

    public void setCherWell(final CherWell cherWell) {
        this.cherWell = cherWell;
    }

    public EverBridge getEverBridge() {
        return everBridge;
    }

    public void setEverBridge(final EverBridge everBridge) {
        this.everBridge = everBridge;
    }

    public Bynder getBynder() {
        return bynder;
    }

    public void setBynder(final Bynder bynder) {
        this.bynder = bynder;
    }

    public Gartner getGartner() {
        return gartner;
    }

    public void setGartner(final Gartner gartner) {
        this.gartner = gartner;
    }

    public Zendesk getZendesk() {
        return zendesk;
    }

    public void setZendesk(final Zendesk zendesk) {
        this.zendesk = zendesk;
    }

    public Slack getSlack() {
        return slack;
    }

    public void setSlack(final Slack slack) {
        this.slack = slack;
    }

    public SecuringTheHuman getSansSth() {
        return sansSth;
    }

    public void setSansSth(final SecuringTheHuman sansSth) {
        this.sansSth = sansSth;
    }

    public InfiniteCampus getInfiniteCampus() {
        return infiniteCampus;
    }

    public void setInfiniteCampus(final InfiniteCampus infiniteCampus) {
        this.infiniteCampus = infiniteCampus;
    }

    public EasyIep getEasyIep() {
        return easyIep;
    }

    public void setEasyIep(final EasyIep easyIep) {
        this.easyIep = easyIep;
    }

    public AcademicWorks getAcademicWorks() {
        return academicWorks;
    }

    public void setAcademicWorks(final AcademicWorks academicWorks) {
        this.academicWorks = academicWorks;
    }

    public AdobeCloud getAdobeCloud() {
        return adobeCloud;
    }

    public void setAdobeCloud(final AdobeCloud adobeCloud) {
        this.adobeCloud = adobeCloud;
    }

    public ArcGIS getArcGIS() {
        return arcGIS;
    }

    public void setArcGIS(final ArcGIS arcGIS) {
        this.arcGIS = arcGIS;
    }

    public OpenAthens getOpenAthens() {
        return openAthens;
    }

    public void setOpenAthens(final OpenAthens openAthens) {
        this.openAthens = openAthens;
    }

    public WebAdvisor getWebAdvisor() {
        return webAdvisor;
    }

    public void setWebAdvisor(final WebAdvisor webAdvisor) {
        this.webAdvisor = webAdvisor;
    }

    public Tableau getTableau() {
        return tableau;
    }

    public void setTableau(final Tableau tableau) {
        this.tableau = tableau;
    }

    public Asana getAsana() {
        return asana;
    }

    public void setAsana(final Asana asana) {
        this.asana = asana;
    }

    public Evernote getEvernote() {
        return evernote;
    }

    public void setEvernote(final Evernote evernote) {
        this.evernote = evernote;
    }

    public Zoom getZoom() {
        return zoom;
    }

    public void setZoom(final Zoom zoom) {
        this.zoom = zoom;
    }

    public InCommon getInCommon() {
        return inCommon;
    }

    public void setInCommon(final InCommon inCommon) {
        this.inCommon = inCommon;
    }

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

    public BenefitFocus getBenefitFocus() {
        return benefitFocus;
    }

    public void setBenefitFocus(final BenefitFocus benefitFocus) {
        this.benefitFocus = benefitFocus;
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Dropbox extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -8275173711355379058L;

        public Dropbox() {
            setNameIdAttribute("mail");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Box extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -5320292115253509284L;

        public Box() {
            setAttributes(CollectionUtils.wrapList(EMAIL, "firstName", "lastName"));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class SAManage extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -8695176237527302883L;

        public SAManage() {
            setNameIdAttribute("mail");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Workday extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 3484810792914261584L;

        public Workday() {
            setSignAssertions(true);
            setSignResponses(true);
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Famis extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 4685484530782109454L;
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Salesforce extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 4685484530782109454L;

        public Salesforce() {
            setAttributes(CollectionUtils.wrapList("mail", PRINCIPAL_NAME));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class ServiceNow extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 4329681021653966734L;

        public ServiceNow() {
            setAttributes(CollectionUtils.wrap(PRINCIPAL_NAME));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class NetPartner extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 5262806306575955633L;

        public NetPartner() {
            setNameIdAttribute("studentId");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Office365 extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 5878458463269060163L;

        public Office365() {
            setNameIdAttribute("scopedImmutableID");
            setAttributes(CollectionUtils.wrapList("IDPEmail", "ImmutableID"));
            setSignResponses(false);
            setSignAssertions(true);
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class WebAdvisor extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 8449304623099588610L;

        public WebAdvisor() {
            setAttributes(CollectionUtils.wrap("uid"));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Webex extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 1957066095836617091L;

        public Webex() {
            setNameIdAttribute(EMAIL);
            setAttributes(CollectionUtils.wrapList("firstName", "lastName"));
            setSignResponses(false);
            setSignAssertions(true);
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Tableau extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -2426590644028989950L;

        public Tableau() {
            setAttributes(CollectionUtils.wrap("username"));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class TestShib extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -622256214333755377L;

        public TestShib() {
            //setMetadata("http://www.testshib.org/metadata/testshib-providers.xml");
            setAttributes(CollectionUtils.wrap(PRINCIPAL_NAME));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Zoom extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -4877129302021248398L;

        public Zoom() {
            setNameIdAttribute("mail");
            setAttributes(CollectionUtils.wrapList("mail", "sn", "givenName"));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class ArcGIS extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 2976006720801066953L;

        public ArcGIS() {
            setNameIdAttribute("arcNameId");
            setAttributes(CollectionUtils.wrapList("mail", "givenName", "arcNameId"));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class InCommon extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6336757169059216490L;

        public InCommon() {
            //setMetadata("http://md.incommon.org/InCommon/InCommon-metadata.xml");
            //setSignatureLocation("/etc/cas/config/certs/inc-md-cert.pem");
            setAttributes(CollectionUtils.wrap(PRINCIPAL_NAME));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Evernote extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -1333379518527897627L;

        public Evernote() {
            setNameIdAttribute(EMAIL);
            setNameIdFormat("emailAddress");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Asana extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 6392492484052314295L;

        public Asana() {
            setNameIdAttribute(EMAIL);
            setNameIdFormat("emailAddress");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class OpenAthens extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 7295249577313928465L;

        public OpenAthens() {
            //setMetadata("https://login.openathens.net/saml/2/metadata-sp");
            setAttributes(CollectionUtils.wrapList(PRINCIPAL_NAME, EMAIL));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class BenefitFocus extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6518570556068267724L;

        public BenefitFocus() {
            setNameIdAttribute("benefitFocusUniqueId");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class AdobeCloud extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -5466434234795577247L;

        public AdobeCloud() {
            setAttributes(CollectionUtils.wrapList("firstName", "lastName", EMAIL));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class AcademicWorks extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 5855725238963607605L;

        public AcademicWorks() {
            setAttributes(CollectionUtils.wrapList("displayName", EMAIL));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class EasyIep extends AbstractSamlSPProperties {
        private static final long serialVersionUID = 6177866628049579956L;

        public EasyIep() {
            setAttributes(CollectionUtils.wrap("employeeId"));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class InfiniteCampus extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -9023417844664430533L;

        public InfiniteCampus() {
            setAttributes(CollectionUtils.wrap("employeeId"));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class SecuringTheHuman extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -1688194227471468248L;

        public SecuringTheHuman() {
            setAttributes(CollectionUtils.wrapList("firstName", "lastName", EMAIL, "scopedUserId", "department", "reference"));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Slack extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -1996859011579246804L;

        public Slack() {
            setNameIdFormat("persistent");
            setAttributes(CollectionUtils.wrapList("User.Email", "User.Username", "first_name", "last_name"));
            setNameIdAttribute("employeeId");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Zendesk extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -4668960591734555087L;

        public Zendesk() {
            setNameIdFormat("emailAddress");
            setNameIdAttribute("email");
            setAttributes(CollectionUtils.wrapList("organization", "tags", "phone", "role"));
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Bynder extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -3168960591734555088L;

        public Bynder() {
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class CherWell extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -3168960591734555088L;

        public CherWell() {
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class NewRelic extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -3268960591734555088L;

        public NewRelic() {
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Yuja extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -1168960591734555088L;

        public Yuja() {
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Symplicity extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -3178960591734555088L;

        public Symplicity() {
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Egnyte extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -3168760591734555088L;

        public Egnyte() {
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class EverBridge extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -5168960591734555088L;

        public EverBridge() {
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class SunshineStateEdResearchAlliance extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -5558960591734555088L;

        public SunshineStateEdResearchAlliance() {
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    public static class Gartner extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public Gartner() {
            setAttributes("urn:oid:2.5.4.42", "urn:oid:2.5.4.4", "urn:oid:0.9.2342.19200300.100.1.3");
        }
    }
}

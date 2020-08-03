package org.apereo.cas.configuration.model.support.saml.sps;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link SamlServiceProviderProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml-sp-integrations")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class SamlServiceProviderProperties implements Serializable {

    private static final long serialVersionUID = 8602328179113963081L;
    /**
     * Settings related to ConcurSolutions acting as a SAML service provider.
     */
    private ConcurSolutions concurSolutions = new ConcurSolutions();
    /**
     * Settings related to Qualtrics acting as a SAML service provider.
     */
    private Qualtrics qualtrics = new Qualtrics();
    /**
     * Settings related to Emma acting as a SAML service provider.
     */
    private Emma emma = new Emma();
    /**
     * Settings related to CrashPlan acting as a SAML service provider.
     */
    private CrashPlan crashPlan = new CrashPlan();
    /**
     * Settings related to SafariOnline acting as a SAML service provider.
     */
    private SafariOnline safariOnline = new SafariOnline();
    /**
     * Settings related to TopHat acting as a SAML service provider.
     */
    private TopHat topHat = new TopHat();
    /**
     * Settings related to DocuSign acting as a SAML service provider.
     */
    private DocuSign docuSign = new DocuSign();
    /**
     * Settings related to PagerDuty acting as a SAML service provider.
     */
    private PagerDuty pagerDuty = new PagerDuty();
    /**
     * Settings related to JIRA acting as a SAML service provider.
     */
    private Jira jira = new Jira();
    /**
     * Settings related to PollEverywhere acting as a SAML service provider.
     */
    private PollEverywhere pollEverywhere = new PollEverywhere();
    /**
     * Settings related to Hipchat acting as a SAML service provider.
     */
    private Hipchat hipchat = new Hipchat();
    /**
     * Settings related to Gitlab acting as a SAML service provider.
     */
    private Gitlab gitlab = new Gitlab();
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
     * Settings related to Confluence acting as a SAML service provider.
     */
    private Confluence confluence = new Confluence();
    /**
     * Settings related to Zimbra acting as a SAML service provider.
     */
    private Zimbra zimbra = new Zimbra();
    /**
     * Settings related to Symplicity acting as a SAML service provider.
     */
    private Symplicity symplicity = new Symplicity();
    /**
     * Settings related to AppDynamics acting as a SAML service provider.
     */
    private AppDynamics appDynamics = new AppDynamics();
    /**
     * Settings related to Amazon acting as a SAML service provider.
     */
    private Amazon amazon = new Amazon();
    /**
     * Settings related to BlackBaud acting as a SAML service provider.
     */
    private BlackBaud blackBaud = new BlackBaud();
    /**
     * Settings related to GiveCampus acting as a SAML service provider.
     */
    private GiveCampus giveCampus = new GiveCampus();
    /**
     * Settings related to WarpWire acting as a SAML service provider.
     */
    private WarpWire warpWire = new WarpWire();
    /**
     * Settings related to RocketChat acting as a SAML service provider.
     */
    private RocketChat rocketChat = new RocketChat();
    /**
     * Settings related to ArmsSoftware acting as a SAML service provider.
     */
    private ArmsSoftware armsSoftware = new ArmsSoftware();
    /**
     * Settings related to Academic HealthPlans acting as a SAML service provider.
     */
    private AcademicHealthPlans academicHealthPlans = new AcademicHealthPlans();
    /**
     * Settings related to NeoGov acting as a SAML service provider.
     */
    private NeoGov neoGov = new NeoGov();
    /**
     * Settings related to Cranium Cafe acting as a SAML service provider.
     */
    private CraniumCafe craniumCafe = new CraniumCafe();
    /**
     * Settings related to CCC acting as a SAML service provider.
     */
    private CaliforniaCommunityColleges cccco = new CaliforniaCommunityColleges();

    @Getter
    private enum CommonAttributeNames {
        /**
         * Attribute name.
         */
        EDU_PERSON_PRINCIPAL_NAME("eduPersonPrincipalName"),
        /**
         * Attribute name.
         */
        EDU_PERSON_PRIMARY_AFFILIATION("eduPersonPrimaryAffiliation"),
        /**
         * Attribute name.
         */
        EDU_PERSON_AFFILIATION("eduPersonAffiliation"),
        /**
         * Attribute name.
         */
        EDU_PERSON_SCOPED_AFFILIATION("eduPersonScopedAffiliation"),
        /**
         * Attribute name.
         */
        GIVEN_NAME("givenName"),
        /**
         * Attribute name.
         */
        DISPLAY_NAME("displayName"),
        /**
         * Attribute name.
         */
        SURNAME("surname"),
        /**
         * Attribute name.
         */
        UID("uid"),
        /**
         * Attribute name.
         */
        COMMON_NAME("commonName"),
        /**
         * Attribute name.
         */
        USERNAME("username"),
        /**
         * Attribute name.
         */
        FIRST_NAME("firstName"),
        /**
         * Attribute name.
         */
        LAST_NAME("lastName"),
        /**
         * Attribute name.
         */
        SN("sn"),
        /**
         * Attribute name.
         */
        CN("cn"),
        /**
         * Attribute name.
         */
        MAIL("mail"),
        /**
         * Attribute name.
         */
        EMPLOYEE_NUMBER("employeeNumber"),
        /**
         * Attribute name.
         */
        STUDENT_ID("studentId"),
        /**
         * Attribute name.
         */
        IMMUTABLE_ID("ImmutableID"),

        /**
         * Attribute name.
         */
        EMAIL("email");

        /**
         * Attribute name.
         */
        private final String attributeName;

        CommonAttributeNames(final String name) {
            this.attributeName = name;
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Dropbox extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -8275173711355379058L;

        public Dropbox() {
            setNameIdAttribute(CommonAttributeNames.MAIL.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Box extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -5320292115253509284L;

        public Box() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.FIRST_NAME.getAttributeName(),
                CommonAttributeNames.LAST_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class SAManage extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -8695176237527302883L;

        public SAManage() {
            setNameIdAttribute(CommonAttributeNames.MAIL.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Workday extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 3484810792914261584L;

        public Workday() {
            setSignAssertions(true);
            setSignResponses(true);
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Famis extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 4685484530782109454L;
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Salesforce extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 4685484530782109454L;

        public Salesforce() {
            addAttributes(CommonAttributeNames.MAIL.getAttributeName(), CommonAttributeNames.EDU_PERSON_PRINCIPAL_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ServiceNow extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 4329681021653966734L;

        public ServiceNow() {
            addAttributes(CommonAttributeNames.EDU_PERSON_PRINCIPAL_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class NetPartner extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 5262806306575955633L;

        public NetPartner() {
            setNameIdAttribute(CommonAttributeNames.STUDENT_ID.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Office365 extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 5878458463269060163L;

        public Office365() {
            setNameIdAttribute("objectGUID");
            addAttributes("IDPEmail", CommonAttributeNames.IMMUTABLE_ID.getAttributeName());
            setSignResponses(false);
            setSignAssertions(true);
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class WebAdvisor extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 8449304623099588610L;

        public WebAdvisor() {
            addAttributes(CommonAttributeNames.UID.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Webex extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 1957066095836617091L;

        public Webex() {
            setNameIdAttribute(CommonAttributeNames.EMAIL.getAttributeName());
            addAttributes(CommonAttributeNames.FIRST_NAME.getAttributeName(), CommonAttributeNames.LAST_NAME.getAttributeName());
            setSignResponses(false);
            setSignAssertions(true);
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Tableau extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -2426590644028989950L;

        public Tableau() {
            addAttributes(CommonAttributeNames.USERNAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Zoom extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -4877129302021248398L;

        public Zoom() {
            setNameIdAttribute(CommonAttributeNames.MAIL.getAttributeName());
            addAttributes(CommonAttributeNames.MAIL.getAttributeName(),
                CommonAttributeNames.SN.getAttributeName(), CommonAttributeNames.GIVEN_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ArcGIS extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 2976006720801066953L;

        public ArcGIS() {
            setNameIdAttribute("arcNameId");
            addAttributes(CommonAttributeNames.MAIL.getAttributeName(), CommonAttributeNames.GIVEN_NAME.getAttributeName(), "arcNameId");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class InCommon extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6336757169059216490L;

        /**
         * InCommon metadata should
         * be available <a href="http://md.incommon.org/InCommon/InCommon-metadata.xml">here</a>.
         */
        public InCommon() {
            addAttributes(CommonAttributeNames.EDU_PERSON_PRINCIPAL_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Evernote extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -1333379518527897627L;

        public Evernote() {
            setNameIdAttribute(CommonAttributeNames.EMAIL.getAttributeName());
            setNameIdFormat("emailAddress");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Asana extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 6392492484052314295L;

        public Asana() {
            setNameIdAttribute(CommonAttributeNames.EMAIL.getAttributeName());
            setNameIdFormat("emailAddress");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class OpenAthens extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 7295249577313928465L;

        public OpenAthens() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(), CommonAttributeNames.EDU_PERSON_PRINCIPAL_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class BenefitFocus extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6518570556068267724L;

        public BenefitFocus() {
            setNameIdAttribute("benefitFocusUniqueId");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class AdobeCloud extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -5466434234795577247L;

        public AdobeCloud() {
            addAttributes(CommonAttributeNames.FIRST_NAME.getAttributeName(), CommonAttributeNames.LAST_NAME.getAttributeName(),
                CommonAttributeNames.EMAIL.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class AcademicWorks extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 5855725238963607605L;

        public AcademicWorks() {
            addAttributes(CommonAttributeNames.DISPLAY_NAME.getAttributeName(), CommonAttributeNames.EMAIL.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class EasyIep extends AbstractSamlSPProperties {

        private static final long serialVersionUID = 6177866628049579956L;

        public EasyIep() {
            addAttributes("employeeId");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class InfiniteCampus extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -9023417844664430533L;

        public InfiniteCampus() {
            addAttributes("employeeId");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class SecuringTheHuman extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -1688194227471468248L;

        public SecuringTheHuman() {
            addAttributes(CommonAttributeNames.FIRST_NAME.getAttributeName(), CommonAttributeNames.LAST_NAME.getAttributeName(),
                CommonAttributeNames.EMAIL.getAttributeName(), "scopedUserId", "department", "reference");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Slack extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -1996859011579246804L;

        public Slack() {
            setNameIdFormat("persistent");
            addAttributes("User.Email", "User.Username", "first_name", "last_name");
            setNameIdAttribute("employeeId");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Zendesk extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -4668960591734555087L;

        public Zendesk() {
            setNameIdFormat("emailAddress");
            setNameIdAttribute("email");
            addAttributes("organization", "tags", "phone", "role");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bynder extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -3168960591734555088L;
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CherWell extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -3168960591734555088L;
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class NewRelic extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -3268960591734555088L;
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Yuja extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -1168960591734555088L;
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Symplicity extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -3178960591734555088L;
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Egnyte extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -3168760591734555088L;
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class EverBridge extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -5168960591734555088L;
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class SunshineStateEdResearchAlliance extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -5558960591734555088L;
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Gartner extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public Gartner() {
            addAttributes("urn:oid:2.5.4.42", "urn:oid:2.5.4.4", "urn:oid:0.9.2342.19200300.100.1.3");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Gitlab extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public Gitlab() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(), "last_name", "first_name", "name");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Hipchat extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public Hipchat() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(), "last_name", "first_name", "title");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class AppDynamics extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public AppDynamics() {
            addAttributes("User.OpenIDName", "User.email", "User.fullName", "AccessControl", "Groups-Membership");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Amazon extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public Amazon() {
            setSignAssertions(true);
            setSignResponses(false);
            addAttributes("awsRoles", "awsRoleSessionName");
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class PollEverywhere extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public PollEverywhere() {
            setSignAssertions(true);
            setSignResponses(false);
            setNameIdAttribute(CommonAttributeNames.EMAIL.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ConcurSolutions extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public ConcurSolutions() {
            setSignAssertions(true);
            setSignResponses(false);
            setNameIdAttribute(CommonAttributeNames.EMAIL.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class BlackBaud extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public BlackBaud() {
            setSignAssertions(true);
            setSignResponses(false);
            addAttributes(CommonAttributeNames.EDU_PERSON_PRINCIPAL_NAME.getAttributeName(), CommonAttributeNames.EMAIL.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class GiveCampus extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public GiveCampus() {
            setSignAssertions(true);
            setSignResponses(false);
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.SURNAME.getAttributeName(),
                CommonAttributeNames.GIVEN_NAME.getAttributeName(),
                CommonAttributeNames.DISPLAY_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class RocketChat extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public RocketChat() {
            setSignAssertions(true);
            setSignResponses(false);
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.CN.getAttributeName(),
                CommonAttributeNames.USERNAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class WarpWire extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public WarpWire() {
            setSignAssertions(true);
            setSignResponses(false);
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.SURNAME.getAttributeName(),
                CommonAttributeNames.GIVEN_NAME.getAttributeName(),
                CommonAttributeNames.EMPLOYEE_NUMBER.getAttributeName(),
                CommonAttributeNames.EDU_PERSON_SCOPED_AFFILIATION.getAttributeName(),
                CommonAttributeNames.EDU_PERSON_PRINCIPAL_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ArmsSoftware extends AbstractSamlSPProperties {

        private static final long serialVersionUID = -6141931806328699054L;

        public ArmsSoftware() {
            setEntityIds(List.of("https://sso.armssoftware.com/sp/shibboleth"));

            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.UID.getAttributeName(),
                CommonAttributeNames.EDU_PERSON_PRINCIPAL_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class AcademicHealthPlans extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public AcademicHealthPlans() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.SURNAME.getAttributeName(),
                CommonAttributeNames.STUDENT_ID.getAttributeName(),
                CommonAttributeNames.GIVEN_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class NeoGov extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public NeoGov() {
            setEntityIds(List.of("https://login.neogov.com/"));
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.IMMUTABLE_ID.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CraniumCafe extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public CraniumCafe() {
            setEntityIds(List.of("https://my.craniumcafe.com/login/saml2"));
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.EDU_PERSON_PRINCIPAL_NAME.getAttributeName(),
                CommonAttributeNames.EDU_PERSON_SCOPED_AFFILIATION.getAttributeName(),
                CommonAttributeNames.STUDENT_ID.getAttributeName(),
                CommonAttributeNames.DISPLAY_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CaliforniaCommunityColleges extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public CaliforniaCommunityColleges() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.UID.getAttributeName(),
                CommonAttributeNames.GIVEN_NAME.getAttributeName(),
                CommonAttributeNames.COMMON_NAME.getAttributeName(),
                CommonAttributeNames.SURNAME.getAttributeName(),
                CommonAttributeNames.EDU_PERSON_PRINCIPAL_NAME.getAttributeName(),
                CommonAttributeNames.EDU_PERSON_PRIMARY_AFFILIATION.getAttributeName(),
                CommonAttributeNames.EDU_PERSON_SCOPED_AFFILIATION.getAttributeName(),
                CommonAttributeNames.DISPLAY_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Confluence extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public Confluence() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.UID.getAttributeName(),
                CommonAttributeNames.GIVEN_NAME.getAttributeName(),
                CommonAttributeNames.SURNAME.getAttributeName(),
                CommonAttributeNames.DISPLAY_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Jira extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public Jira() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.UID.getAttributeName(),
                CommonAttributeNames.GIVEN_NAME.getAttributeName(),
                CommonAttributeNames.SURNAME.getAttributeName(),
                CommonAttributeNames.DISPLAY_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CrashPlan extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public CrashPlan() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.GIVEN_NAME.getAttributeName(),
                CommonAttributeNames.SURNAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class DocuSign extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public DocuSign() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.GIVEN_NAME.getAttributeName(),
                CommonAttributeNames.EMPLOYEE_NUMBER.getAttributeName(),
                CommonAttributeNames.SURNAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class SafariOnline extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public SafariOnline() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.GIVEN_NAME.getAttributeName(),
                CommonAttributeNames.EMPLOYEE_NUMBER.getAttributeName(),
                CommonAttributeNames.EDU_PERSON_AFFILIATION.getAttributeName(),
                CommonAttributeNames.SURNAME.getAttributeName());
        }
    }


    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class PagerDuty extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public PagerDuty() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Zimbra extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public Zimbra() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class TopHat extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public TopHat() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.EDU_PERSON_PRINCIPAL_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Emma extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public Emma() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.SURNAME.getAttributeName(),
                CommonAttributeNames.GIVEN_NAME.getAttributeName());
        }
    }

    @RequiresModule(name = "cas-server-support-saml-sp-integrations")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Qualtrics extends AbstractSamlSPProperties {
        private static final long serialVersionUID = -6141931806328699054L;

        public Qualtrics() {
            addAttributes(CommonAttributeNames.EMAIL.getAttributeName(),
                CommonAttributeNames.SURNAME.getAttributeName(),
                CommonAttributeNames.EDU_PERSON_PRINCIPAL_NAME.getAttributeName(),
                CommonAttributeNames.EMPLOYEE_NUMBER.getAttributeName(),
                CommonAttributeNames.GIVEN_NAME.getAttributeName());
        }
    }
}

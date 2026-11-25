const assert = require("assert");
const path = require("path");
const {processors} = require("xml2js");
const cas = require("../../cas.js");

const UNSOLICITED_SSO_URL = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
const ACS_URL = "https://localhost:9859/post";
const STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";

const PRINCIPAL_ATTRIBUTES = ["mail", "givenName", "eduPersonPrincipalName", "telephoneNumber", "title"];
const ENTITY_ATTRIBUTE_BUNDLE = ["mail", "telephoneNumber"];
const REFEDS_RS_BUNDLE = ["mail", "givenName", "eduPersonPrincipalName"];

function textOf(element) {
    return typeof element === "string" ? element : element._;
}

async function loginIfNeeded(page) {
    await cas.sleep(2000);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(3000);
    }
    await cas.logPage(page);
}

async function releasedAttributes(page, entityId) {
    await cas.goto(page, `${UNSOLICITED_SSO_URL}?providerId=${encodeURIComponent(entityId)}`);
    await loginIfNeeded(page);
    assert(page.url().startsWith(ACS_URL), `Response for ${entityId} must be posted to ${ACS_URL}, but page is at ${page.url()}`);
    const content = JSON.parse(await cas.innerText(page, "body"));
    assert(content.form.SAMLResponse !== undefined);
    const xml = await cas.base64Decode(content.form.SAMLResponse);
    await cas.log(xml);
    const response = (await cas.parseXML(xml, {tagNameProcessors: [processors.stripPrefix]})).Response;
    assert(response.Status[0].StatusCode[0].$.Value === STATUS_SUCCESS);
    const assertion = response.Assertion[0];
    assert(textOf(assertion.Conditions[0].AudienceRestriction[0].Audience[0]) === entityId);
    const attributes = {};
    (assertion.AttributeStatement ?? [])
        .flatMap((statement) => statement.Attribute ?? [])
        .forEach((attribute) => {
            attributes[attribute.$.FriendlyName ?? attribute.$.Name] = (attribute.AttributeValue ?? []).map(textOf);
        });
    await cas.log(`Attributes released to ${entityId}: ${JSON.stringify(attributes)}`);
    return attributes;
}

function assertReleased(attributes, entityId, expected) {
    const released = PRINCIPAL_ATTRIBUTES.filter((name) => attributes[name] !== undefined);
    assert.deepStrictEqual(released.sort(), [...expected].sort(),
        `${entityId} must receive exactly ${expected}, but received ${released}`);
    if (expected.includes("mail")) {
        assert.deepStrictEqual(attributes.mail, ["casuser@example.org"]);
    }
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    let success = false;
    try {
        const context = await browser.createBrowserContext();
        const page = await cas.newPage(context);

        await cas.logg("The entity attribute in metadata authorizes the release");
        let entityId = "https://localhost:9859/entity-attributes/category";
        assertReleased(await releasedAttributes(page, entityId), entityId, ENTITY_ATTRIBUTE_BUNDLE);

        await cas.logg("Without the entity attribute, an entity id matching an entityIds pattern authorizes the release");
        entityId = "https://localhost:9859/entity-attributes/trusted/app";
        assertReleased(await releasedAttributes(page, entityId), entityId, ENTITY_ATTRIBUTE_BUNDLE);

        await cas.logg("An entity id listed literally in entityIds authorizes the release");
        entityId = "https://localhost:9859/entity-attributes/literal";
        assertReleased(await releasedAttributes(page, entityId), entityId, ENTITY_ATTRIBUTE_BUNDLE);

        await cas.logg("Another value of the entity attribute and an entity id matching no pattern release nothing");
        entityId = "https://localhost:9859/entity-attributes/other";
        assertReleased(await releasedAttributes(page, entityId), entityId, []);

        await cas.logg("The REFEDS R&S bundle is released to an entity id matching the pattern, without the entity category");
        entityId = "https://localhost:9859/refeds-rs/trusted/app";
        assertReleased(await releasedAttributes(page, entityId), entityId, REFEDS_RS_BUNDLE);

        await cas.logg("The REFEDS R&S bundle is withheld from an entity id that matches no pattern");
        entityId = "https://localhost:9859/refeds-rs/other";
        assertReleased(await releasedAttributes(page, entityId), entityId, []);

        await context.close();
        success = true;
    } finally {
        if (success) {
            await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        }
        await cas.closeBrowser(browser);
    }
})();

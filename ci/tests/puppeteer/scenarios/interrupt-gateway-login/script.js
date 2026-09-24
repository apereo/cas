
const assert = require("assert");
const path = require("path");
const cas = require("../../cas.js");

const casService = "https://localhost:9859/anything/cas";
const oidcRedirectUri = "https://localhost:9859/post";
const samlServiceProvider = "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp";
const state = "Rk9JC5nzPx1aTLfW3dYq";
const nonce = "b8Hs2kVw4Q";

async function casGateway(page) {
    await cas.log("Sending CAS gateway request...");
    await cas.goto(page, `https://localhost:8443/cas/login?service=${casService}&gateway=true`);
    await cas.sleep(2000);
    await cas.logPage(page);
    await cas.assertElementDoesNotExist(page, "#interruptMessage");
    await cas.assertPageUrlStartsWith(page, casService);
}

async function oidcPromptNone(page) {
    await cas.log("Sending OpenID Connect authorization request with prompt=none...");
    const url = "https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid"
        + `&prompt=none&response_mode=form_post&redirect_uri=${oidcRedirectUri}&state=${state}&nonce=${nonce}`;
    await cas.goto(page, url);
    await cas.sleep(2000);
    await cas.logPage(page);
    await cas.assertElementDoesNotExist(page, "#interruptMessage");
    await page.waitForSelector("body pre", {visible: true});
    const payload = JSON.parse(await cas.textContent(page, "body pre"));
    assert(payload.form.state === state);
    return payload.form;
}

async function samlPassive(page) {
    await cas.log("Sending SAML2 passive authentication request...");
    await cas.goto(page, samlServiceProvider);
    await cas.sleep(3000);
    await cas.logPage(page);
    await cas.screenshot(page);
    await cas.assertElementDoesNotExist(page, "#interruptMessage");
    await cas.assertPageUrlStartsWith(page, "http://localhost:9443/simplesaml/");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Passive requests without a single sign-on session are never interrupted");
    await casGateway(page);
    await cas.assertTicketParameter(page, false);

    let result = await oidcPromptNone(page);
    assert(result.error === "login_required");
    assert(result.code === undefined);

    await samlPassive(page);
    await cas.assertElementDoesNotExist(page, "#table_with_attributes");

    await cas.log("Establishing SSO session; the interrupt is presented but not acknowledged");
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContent(page, "#interruptMessage", "Please review the updated terms of use");
    await cas.assertCookie(page);

    await cas.log("Passive requests that return to the login flow are not interrupted and receive no ticket");
    await casGateway(page);
    await cas.assertTicketParameter(page, false);

    result = await oidcPromptNone(page);
    assert(result.error === "login_required");
    assert(result.code === undefined);

    await cas.log("SAML2 passive requests are answered from the SSO session without the login flow, so no interrupt runs");
    await samlPassive(page);
    await page.waitForSelector("#table_with_attributes", {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");

    await cas.log("Interactive request still presents the interrupt, which is now acknowledged");
    await cas.gotoLogin(page, casService);
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#interruptMessage", "Please review the updated terms of use");
    await cas.submitForm(page, "#fm1");
    await cas.sleep(2000);
    await cas.assertTicketParameter(page);

    await casGateway(page);
    await cas.assertTicketParameter(page);

    result = await oidcPromptNone(page);
    assert(result.error === undefined);
    assert(result.code !== undefined);
    assert(result.nonce === nonce);

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

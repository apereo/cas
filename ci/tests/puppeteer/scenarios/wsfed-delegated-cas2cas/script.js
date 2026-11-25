const assert = require("assert");
const cas = require("../../cas.js");

const REALM = "urn:org:apereo:cas:ws:idp:realm-CAS";
const SERVICE = "https://localhost:9859/anything/wsfedcas2cas";
const RELYING_PARTY_LOGIN = "https://localhost:8443/cas/login";
const IDENTITY_PROVIDER_CALLBACK = "https://localhost:8444/cas/ws/idp/federationcallback";

(async () => {
    await cas.log("The identity provider refuses a sign-in request that does not name its reply address");
    await cas.doRequest(`https://localhost:8444/cas/ws/idp/federation?wa=wsignin1.0&wtrealm=${encodeURIComponent(REALM)}`, "GET", {}, 400);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page, undefined, 8443);
    await cas.gotoLogout(page, undefined, 8444);

    await cas.log("Starting at the relying party, which redirects to the CAS WS-Federation identity provider");
    await cas.gotoLogin(page, SERVICE);
    await cas.sleep(3000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8444/cas/login");
    const loginUrl = new URL(page.url());
    assert(loginUrl.searchParams.getAll("wreply").includes(RELYING_PARTY_LOGIN));
    assert(loginUrl.searchParams.getAll("wtrealm").includes(REALM));
    const callback = loginUrl.searchParams.getAll("service").find((value) => value.startsWith(IDENTITY_PROVIDER_CALLBACK));
    assert(callback !== undefined, "The identity provider login must ask for a ticket for its federation callback");
    assert.equal(new URL(callback).searchParams.get("wreply"), RELYING_PARTY_LOGIN);

    await cas.log("The identity provider service defines no appliesTo, so the token audience must default to the realm");
    await cas.loginWith(page);
    await cas.sleep(5000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, SERVICE);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(SERVICE, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert.equal(authenticationSuccess.user, "casuser");
    assert.deepStrictEqual(authenticationSuccess.attributes.upn, ["casuser"]);
    assert.deepStrictEqual(authenticationSuccess.attributes.givenname, ["CAS"]);
    assert.deepStrictEqual(authenticationSuccess.attributes.emailaddress, ["casuser@example.org"]);
    await cas.log("Markup characters in a released claim survive the token post back to the relying party");
    assert.deepStrictEqual(authenticationSuccess.attributes.surname, ["Smith&Sons<LLC>"]);
    assert.equal(authenticationSuccess.attributes.credentialType[0], "WsFederationCredential");

    await cas.log("The relying party now holds its own single sign-on session");
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page, true, "TGC1");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    await cas.gotoLogin(page, SERVICE);
    await cas.sleep(2000);
    await cas.assertPageUrlStartsWith(page, SERVICE);
    const ssoTicket = await cas.assertTicketParameter(page);
    const ssoJson = await cas.validateTicket(SERVICE, ssoTicket);
    assert.equal(ssoJson.serviceResponse.authenticationSuccess.user, "casuser");
    assert.equal(ssoJson.serviceResponse.authenticationSuccess.attributes.credentialType[0], "WsFederationCredential");

    await cas.closeBrowser(browser);
})();

const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

async function fetchIdentityProviders() {
    await cas.doGet("https://localhost:8443/cas/actuator/delegatedClients",
        res => {
            assert(res.status === 200);
            assert(res.data.CasClient !== null);
            assert(res.data.OidcClient !== null);
        },
        error => {
            throw error;
        })
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let properties = {
        "cas.authn.pac4j.cas[0].login-url": "https://localhost:8444/cas/login",
        "cas.authn.pac4j.cas[0].protocol": "CAS30",
        "cas.authn.pac4j.cas[0].client-name": "CasClient",

        "cas.authn.pac4j.oidc[0].generic.id": "0oau8gzwkc00Ww8a30h7",
        "cas.authn.pac4j.oidc[0].generic.secret": "DSWj3msY87WuNWwdn3dhNxLxg4jt4j8MwwClGIAD",
        "cas.authn.pac4j.oidc[0].generic.discovery-uri": "https://dev-968370-admin.oktapreview.com/oauth2/default/.well-known/openid-configuration",
        "cas.authn.pac4j.oidc[0].generic.client-name": "OidcClient"
    };
    let payload = {
        "/delegatedauthn": {
            "get": properties
        }
    };
    let mockServer = await cas.mockJsonServer(payload, 5432);
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await page.waitForTimeout(3000);
    await cas.assertVisibility(page, '#loginProviders');
    await cas.assertVisibility(page, 'li #CasClient');
    await cas.assertVisibility(page, 'li #OidcClient');
    await cas.log("Wait for the cache to expire and reload providers again...");
    await page.waitForTimeout(3000);
    await cas.gotoLogin(page);
    await fetchIdentityProviders();
    await cas.doRequest("https://localhost:8443/cas/actuator/delegatedClients", "DELETE");
    await fetchIdentityProviders();
    mockServer.stop();
    await browser.close();
})();

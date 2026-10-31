const assert = require("assert");
const fs = require("fs");
const path = require("path");
const cas = require("../../cas.js");

const SERVICE = "https://localhost:9859/anything/spnego";
const TICKET_CACHE_DIR = process.env.KERBEROS_TICKET_CACHE_DIR ?? path.join(process.env.TMPDIR ?? "/tmp", "cas-kerberos");

async function launchBrowser(ticketCache) {
    const options = cas.browserOptions();
    return cas.newBrowser({
        ...options,
        args: [
            ...options.args,
            "--auth-server-allowlist=localhost",
            "--disable-auth-negotiate-cname-lookup"
        ],
        env: {
            ...process.env,
            KRB5_CONFIG: path.join(__dirname, "krb5.conf"),
            KRB5CCNAME: `FILE:${ticketCache}`
        }
    });
}

async function verifyLoginFormWithoutKerberosTicket() {
    const browser = await launchBrowser(path.join(TICKET_CACHE_DIR, "missing.ccache"));
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, SERVICE);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#username");
    await cas.assertCookie(page, false);
    await cas.loginWith(page);
    await cas.sleep(1000);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(SERVICE, ticket);
    const success = json.serviceResponse.authenticationSuccess;
    assert.equal(success.user, "casuser");
    assert.equal(success.attributes.credentialType[0], "UsernamePasswordCredential");
    await cas.gotoLogout(page);
    await cas.closeBrowser(browser);
}

async function verifyKerberosLogin() {
    const ticketCache = path.join(TICKET_CACHE_DIR, "casuser.ccache");
    assert(fs.existsSync(ticketCache), `Kerberos ticket cache ${ticketCache} does not exist`);
    const browser = await launchBrowser(ticketCache);
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, SERVICE);
    await cas.sleep(2000);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(SERVICE, ticket);
    const success = json.serviceResponse.authenticationSuccess;
    const expectedUser = process.env.SCENARIO_VARIATION === "principal-with-domain-name" ? "casuser@EXAMPLE.ORG" : "casuser";
    assert.equal(success.user, expectedUser);
    assert.equal(success.attributes.credentialType[0], "SpnegoCredential");
    assert.equal(success.attributes.successfulAuthenticationHandlers[0], "SPNEGO");

    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.closeBrowser(browser);
}

(async () => {
    await verifyLoginFormWithoutKerberosTicket();
    await verifyKerberosLogin();
})();

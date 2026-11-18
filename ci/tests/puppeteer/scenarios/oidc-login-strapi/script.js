
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.assertCookie(page);
        await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
        const response = await cas.goto(page, "http://localhost:1337/api/connect/cas");
        assert(response !== null, "Strapi login did not return a response");
        const body = await response.json();
        assert.strictEqual(response.status(), 200, `Strapi login failed: ${JSON.stringify(body.error)}`);
        assert.strictEqual(new URL(response.url()).pathname, "/api/auth/cas/callback");
        assert(typeof body.jwt === "string" && body.jwt.length > 0, "Strapi did not issue a JWT");
        assert.strictEqual(body.user.username, "casuser");
        assert.strictEqual(body.user.email, "casuser@apereo.org");
        assert.strictEqual(body.user.provider, "cas");
        assert.strictEqual(body.user.confirmed, true);
    } finally {
        await cas.closeBrowser(browser);
    }
})();

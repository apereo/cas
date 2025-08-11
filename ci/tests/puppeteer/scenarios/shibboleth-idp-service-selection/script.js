
const cas = require("../../cas.js");

function getShibbolethUrlForEntityId(entityId) {
    return `https://localhost:8443/cas/login?locale=en&service=https%3A%2F%2Fsso.shibboleth.org%2Fidp%2FAuthn%2FExternal%3Fconversation%3De1s1&entityId=${entityId}`;
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, getShibbolethUrlForEntityId("google.com"));
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertInnerTextStartsWith(page, "#login h2", "Your account is not registered");
    await cas.assertVisibility(page, "img#imageQRCode");
    await cas.assertVisibility(page, "#scratchcodes");

    await cas.goto(page, getShibbolethUrlForEntityId("github.com"));
    await cas.loginWith(page);
    await cas.sleep(5000);
    await cas.assertVisibility(page, "#token");
    await cas.assertVisibility(page, "#resendButton");
    await cas.assertVisibility(page, "#loginButton");

    await cas.closeBrowser(browser);
})();

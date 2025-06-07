const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    await cas.httpServer(__dirname, 5432, false);
    let failure = false;
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        const redirectUri = "https://localhost:9859/anything/1";
        const url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
            `client_id=client&redirect_uri=${decodeURIComponent(redirectUri)}&` +
            "scope=openid%20email%20profile%20address%20phone&response_type=code";

        await cas.goto(page, url);

        await cas.assertVisibility(page, "li #KEYCLOAK");
        await cas.click(page, "li #KEYCLOAK");
        await cas.waitForNavigation(page);

        await cas.sleep(3000);
        await cas.screenshot(page);

        await cas.loginWith(page, "caskeycloak", "r2RlZXz6f2h5");
        await cas.sleep(1000);

        const result = new URL(page.url());
        await cas.log(result.searchParams.toString());

        assert(result.searchParams.has("ticket") === false);
        assert(result.searchParams.has("client_id"));
        assert(result.searchParams.has("redirect_uri"));
        assert(result.searchParams.has("scope"));
        await cas.sleep(2000);

        await cas.log("Allowing release of scopes and claims...");
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
        await cas.sleep(2000);

        await cas.logPage(page);
        await cas.assertPageUrlStartsWith(page, "https://localhost:9859/anything/1");
        await cas.sleep(2000);
        const body = JSON.parse(await cas.innerText(page, "pre"));
        assert(body.args.code !== null);

        await cas.gotoLogout(page);
    } catch (e) {
        failure = true;
        throw e;
    } finally {
        await browser.close();
        if (!failure) {
            await process.exit(0);
        }
    }
})();

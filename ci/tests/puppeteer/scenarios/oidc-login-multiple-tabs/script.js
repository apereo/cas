const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const app1 = "http://localhost:9889/anything/app";
    const app2 = "http://localhost:9889/anything/app2";
    const url = ({redirectUri, clientId}) =>
        `https://localhost:8443/cas/oidc/authorize?response_type=code&redirect_uri=${redirectUri}&client_id=${clientId}&scope=openid&state=9qa3`;
    try {
        const context = await browser.createBrowserContext();

        await cas.log("Start with the first browser tab...");
        const page1 = await cas.newPage(context, true);
        await page1.bringToFront();
        await cas.goto(page1, url({redirectUri: app1, clientId: "client"}));
        await cas.logPage(page1);
        await cas.sleep(2000);

        await cas.log("Continue with the second browser tab...");
        const page2 = await cas.newPage(context, true);
        await page2.bringToFront();
        await cas.goto(page2, url({redirectUri: app2, clientId: "client2"}));
        await cas.sleep(2000);
        await cas.logPage(page2);
        await cas.loginWith(page2);
        await cas.sleep(2000);
        const code1 = await cas.assertParameter(page2, "code");
        await cas.logPage(page1);
        await cas.log(`OAuth code ${code1} from ${page2.url()}`);
        await cas.assertPageUrlStartsWith(page2, app2);
        await cas.sleep(2000);

        await cas.log("Resume with the first browser tab...");
        await page1.bringToFront();
        await cas.loginWith(page1);
        await cas.sleep(2000);
        const code2 = await cas.assertParameter(page2, "code");
        await cas.logPage(page2);
        await cas.log(`OAuth code ${code2} from ${page1.url()}`);
        await cas.assertPageUrlStartsWith(page1, app1);

        await context.close();
    } finally {
        await cas.closeBrowser(browser);
    }
})();

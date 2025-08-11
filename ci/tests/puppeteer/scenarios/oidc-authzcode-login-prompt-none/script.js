
const assert = require("assert");
const cas = require("../../cas.js");

const state = "40W6nJCYWnnPplmAo13Icy";
const nonce = "yYxIingpZy";

async function login(page, redirectUrl, params) {
    let authzUrl = "https://localhost:8443/cas/oidc/authorize?";
    authzUrl += "response_type=code&client_id=client&scope=openid";
    authzUrl += `&prompt=none&redirect_uri=${redirectUrl}&nonce=${nonce}&state=${state}`;

    if (params !== undefined) {
        authzUrl += `&${params}`;
    }
    await cas.log(`Navigating to ${authzUrl}`);
    await cas.goto(page, authzUrl);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);
    
    const redirectUrl = "https://localhost:9859/post";
    await login(page, redirectUrl, "response_mode=form_post");
    await cas.logPage(page);
    await cas.sleep(2000);
    await cas.log("Waiting for page content body to render...");
    await page.waitForSelector("body pre", { visible: true });
    let content = await cas.textContent(page, "body pre");
    let payload = JSON.parse(content);
    assert(payload.args.error !== "login_required");
    assert(payload.args.state !== state);
    assert(payload.form.error === "login_required");
    assert(payload.form.state === state);

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);

    await login(page, redirectUrl, "response_mode=form_post");
    await cas.logPage(page);
    await cas.sleep(2000);
    await cas.log("Waiting for page content body to render...");
    await page.waitForSelector("body pre", { visible: true });

    content = await cas.textContent(page, "body pre");
    payload = JSON.parse(content);
    assert(payload.form.code !== undefined);
    assert(payload.form.state === state);
    assert(payload.form.nonce === nonce);
    await cas.closeBrowser(browser);
})();

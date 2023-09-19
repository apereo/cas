const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

const state = "40W6nJCYWnnPplmAo13Icy";
const nonce = "yYxIingpZy";

async function login(page, redirectUrl, params) {
    await cas.goto(page, "https://localhost:8443/cas/logout");

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
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    let redirectUrl = "https://httpbin.org/post";
    await login(page, redirectUrl, "response_mode=form_post");
    await cas.log(`Page URL: ${page.url()}`);
    await page.waitForTimeout(2000);
    await cas.log("Waiting for page content body to render...");
    await page.waitForSelector('body pre', { visible: true });
    let content = await cas.textContent(page, "body pre");
    const payload = JSON.parse(content);
    assert(payload.args.error !== "login_required");
    assert(payload.args.state !== state);

    assert(payload.form.error === "login_required");
    assert(payload.form.state === state);
    await browser.close();
})();

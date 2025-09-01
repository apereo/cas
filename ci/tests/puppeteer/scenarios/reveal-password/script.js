
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.type(page, "#username", "casuser");
    await cas.type(page, "#password", "Mellon");

    const pwd = await page.$(".pwd");
    let pwdType = await page.evaluate((pwd) => pwd.type, pwd);
    await cas.log(`password input type is ${pwdType}`);
    assert(pwdType === "password");
    const btnPwd = await page.$(".reveal-password");
    let btnPwdState = await page.evaluate((btnPwd) => btnPwd.getAttribute("aria-pressed"), btnPwd);
    assert(btnPwdState === "false");
    await cas.log(`password toggle button aria-pressed is ${btnPwdState}`);
    await cas.attributeValue(page, ".reveal-password", "title", "Toggle password");
    await cas.innerText(page, ".reveal-password .sr-only");

    await cas.log("click button to reveal password");
    await page.click(".reveal-password");
    pwdType = await page.evaluate((pwd) => pwd.type, pwd);
    await cas.log(`password input type is ${pwdType}`);
    assert(pwdType === "text");
    btnPwdState = await page.evaluate((btnPwd) => btnPwd.getAttribute("aria-pressed"), btnPwd);
    await cas.log(`password toggle button aria-pressed is ${btnPwdState}`);
    assert(btnPwdState === "true");

    await cas.log("click button to show password");
    await page.click(".reveal-password");
    pwdType = await page.evaluate((pwd) => pwd.type, pwd);
    await cas.log(`password input type is ${pwdType}`);
    assert(pwdType === "password");
    btnPwdState = await page.evaluate((btnPwd) => btnPwd.getAttribute("aria-pressed"), btnPwd);
    assert(btnPwdState === "false");
    await cas.log(`password toggle button aria-pressed is ${btnPwdState}`);

    await cas.closeBrowser(browser);
})();

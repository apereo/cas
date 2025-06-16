
const cas = require("../../cas.js");
const assert = require("assert");
const os = require("os");
const fs = require("fs");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "http://localhost:8080/cas/login");
    await cas.assertVisibility(page, "#drawerButton");
    await cas.click(page, "#drawerButton");
    await cas.sleep(3000);
    await cas.screenshot(page);
    await cas.assertVisibility(page, "div.container-fluid");

    await cas.gotoLogin(page);
    await page.focus("#username");
    await cas.pressTab(page);
    await page.focus("#password");
    await cas.pressTab(page);
    await cas.screenshot(page);
    await cas.assertVisibility(page, "#usernameValidationMessage");
    await cas.assertVisibility(page, "#passwordValidationMessage");

    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    await cas.sleep(2000);
    assert (await cas.pageVariable(page, "googleAnalyticsTrackingId") !== null);

    await cas.gotoLogout(page);
    await cas.gotoLogin(page, "https://anything-matches-here");
    await cas.screenshot(page);
    await cas.assertVisibility(page, "#wildcardService");

    await cas.gotoLogout(page);
    const loginPage = `
        <html>
            <body>
                <form id="form" action="https://localhost:8443/cas/login?service=https://localhost:9859/post&method=post" method="post">
                    <input type="submit" value="Continue" />
                </form>
                <script>
                    document.getElementById('form').submit();
                </script>
            </body>
        </html>
    `;
    
    const tempDir = os.tmpdir();
    const loginFile = `${tempDir}/postlogin.html`;
    await fs.writeFileSync(loginFile, loginPage);
    await cas.log(`Login page is written to ${loginFile}`);

    await cas.goto(page, `file://${loginFile}`);
    await cas.sleep(2000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    const content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    assert(payload.form.ticket !== undefined);
    await cas.gotoLogout(page);
    await cas.sleep(1000);

    await cas.gotoLogin(page);
    await cas.loginWith(page, "fancyuser", "jleleuâ¬");
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
    await cas.sleep(1000);
    await browser.close();
})();

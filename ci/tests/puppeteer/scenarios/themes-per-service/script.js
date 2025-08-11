
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);

    await cas.assertVisibility(page, "#twitter-link");
    await cas.assertVisibility(page, "#youtube-link");

    const imgs = await page.$$eval("#cas-logo", (imgs) => imgs.map((img) => img.getAttribute("src")));
    const logo = imgs.pop();
    await cas.log(logo);
    await cas.assertTextMatches(logo, /\/cas\/themes\/example\/images\/logo-.*.png/);

    await cas.log("Logging out...");
    await cas.goto(page, `https://localhost:8443/cas/logout?service=${service}`);
    await cas.sleep(1000);

    await cas.assertVisibility(page, "#twitter-link");
    await cas.assertVisibility(page, "#youtube-link");
    
    await cas.assertVisibility(page, "#logoutButton");
    await cas.submitForm(page, "#fm1");

    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/logout");
    await cas.assertCookie(page, false);

    await cas.assertVisibility(page, "#twitter-link");
    await cas.assertVisibility(page, "#youtube-link");
    
    await cas.closeBrowser(browser);
})();

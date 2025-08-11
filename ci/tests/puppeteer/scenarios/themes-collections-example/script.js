
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.sleep(2000);

    await cas.assertVisibility(page, "#twitter-link");
    await cas.assertVisibility(page, "#youtube-link");
    await cas.assertInvisibility(page, "#pmlinks");

    const imgs = await page.$$eval("#cas-logo", (imgs) => imgs.map((img) => img.getAttribute("src")));
    const logo = imgs.pop();
    await cas.log(logo);
    await cas.assertTextMatches(logo, /\/cas\/themes\/example\/images\/logo-.*.png/);

    await cas.gotoLogout(page);
    await cas.sleep(2000);

    await cas.closeBrowser(browser);
})();

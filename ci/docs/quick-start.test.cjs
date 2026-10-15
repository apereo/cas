// Usage: node ci/docs/quick-start.test.cjs /path/to/rendered/documentation
// Install the existing ci/tests/puppeteer dependencies first.
const puppeteer = require("../tests/puppeteer/node_modules/puppeteer");
const fs = require("node:fs");
const http = require("node:http");
const assert = require("node:assert/strict");
const path = require("node:path");
assert(process.argv[2], "Pass the rendered documentation directory (containing development/planning/Quick-Start.html)");
const root = path.resolve(process.argv[2]);
(async () => {
    const server = http.createServer((req, res) => {
        const file = root + new URL(req.url,"http://localhost").pathname.replace(/^\/cas/, "");
        if (!fs.existsSync(file) || fs.statSync(file).isDirectory()) {
            res.writeHead(404).end();
            return;
        }
        res.setHeader("Content-Type", file.endsWith(".js") ? "text/javascript" : file.endsWith(".css") ? "text/css" : file.endsWith(".html") ? "text/html" : "application/octet-stream");
        res.end(fs.readFileSync(file));
    });
    await new Promise((resolve, reject) => {
        server.once("error", reject);
        server.listen(0, "127.0.0.1", resolve);
    });
    let browser;
    try {
        browser = await puppeteer.launch({headless: true});
        const page = await browser.newPage();
        await page.emulateMediaFeatures([{name:"prefers-reduced-motion",value:"reduce"}]);
        const click = async (selector) => {
            await page.$eval(selector, (el) => el.scrollIntoView({block: "center", behavior: "instant"}));
            await page.locator(selector).click();
        };
        const errors = [];
        page.on("pageerror", (error) => errors.push(error.message));
        const base = `http://127.0.0.1:${server.address().port}/cas/development/planning/Quick-Start.html`;
        const response = await page.goto(base, {waitUntil: "networkidle2"});
        assert(response.ok());
        await page.waitForSelector(".quick-start-step");
        assert.equal(await page.$$eval(".quick-start-step", (els)=>els.length),8);
        assert.equal(await page.$$eval(".quick-start-step:not([hidden])",(els)=>els.length),1);
        await click(".quick-start-controls button:last-child");
        assert.equal(await page.$eval("[aria-current=\"step\"]",(el)=>el.textContent),"2. Create an Overlay");
        await click(".quick-start-selection button:first-child");
        assert.equal(await page.$$eval(".quick-start-features input:checked", (inputs) => inputs.length), 11);
        assert.equal(await page.$$eval("[data-quick-feature][hidden]", (blocks) => blocks.length), 0);
        assert.equal(new URL(page.url()).searchParams.get("features").split(",").length, 11);
        assert(await page.$eval(".quick-start-selection button:first-child", (button) => button.disabled));
        await click(".quick-start-selection button:nth-child(2)");
        assert.equal(await page.$$eval(".quick-start-features input:checked", (inputs) => inputs.length), 0);
        assert.equal(await page.$$eval("[data-quick-feature]:not([hidden])", (blocks) => blocks.length), 0);
        assert.equal(new URL(page.url()).searchParams.has("features"), false);
        assert(await page.$eval(".quick-start-selection button:nth-child(2)", (button) => button.disabled));
        for (const name of ["ldap", "oidc", "saml2", "gauth", "webauthn", "duo", "passwordless", "delegation", "interrupt", "redis", "palantir"]) {
            await click(`.quick-start-features input[value="${name}"]`);
        }
        await click(".quick-start-controls button:last-child");
        assert.equal(await page.$$eval("[data-quick-feature][hidden]",(els)=>els.length),0);
        assert.equal(await page.$$eval(".quick-start-step:not([hidden]) [data-quick-feature] h3",(els)=>els.length),4);
        await page.reload({waitUntil:"networkidle2"});
        assert.equal(await page.$$eval(".quick-start-features input:checked",(els)=>els.length),11);
        await click("#cas-quick-start li:nth-child(6) button");
        assert.match(await page.$eval(".quick-start-summary",(el)=>el.textContent),/OpenID Connect.*Interrupt.*Redis tickets.*Palantir/);
        await page.goBack();
        assert.match(await page.$eval("[aria-current=\"step\"]",(el)=>el.textContent),/^3\./);
        await click("#cas-quick-start li:nth-child(8) button");
        assert(await page.$eval(".quick-start-controls button:last-child",(el)=>el.disabled));
        await click(".quick-start-view");
        assert.equal(await page.$$eval(".quick-start-step:not([hidden])",(els)=>els.length),8);
        await page.goto(`${base}#verify-openid-connect`,{waitUntil:"networkidle2"});
        assert.match(await page.$eval("[aria-current=\"step\"]",(el)=>el.textContent),/^7\./);
        assert(await page.$eval("[value=\"oidc\"]",(el)=>el.checked));
        for (const [feature, heading] of [["saml2", "verify-saml2"], ["gauth", "verify-mfa-with-google-authenticator"],
            ["webauthn", "verify-mfa-with-webauthn"], ["duo", "verify-mfa-with-duo-security"], ["passwordless", "verify-passwordless-authentication"],
            ["delegation", "verify-delegated-authentication"]]) {
            await page.goto(`${base}#${heading}`, {waitUntil: "networkidle2"});
            assert(await page.$eval(`[value="${feature}"]`, (input) => input.checked));
            assert.match(await page.$eval("[aria-current='step']", (el) => el.textContent), /^7\./);
        }
        await page.emulateMediaType("print");
        assert.equal(await page.$$eval(".quick-start-step",(els)=>els.filter((el)=>getComputedStyle(el).display!=="none").length),8);
        await page.emulateMediaType("screen");
        await page.setViewport({width:390,height:844});
        await click("#cas-quick-start li:nth-child(2) button");
        assert(await page.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth));

        await page.setViewport({width:1440,height:1000});

        const featureCount = await page.$$eval(".quick-start-features input", (inputs) => inputs.length);
        const masks = new Set([0, (1 << featureCount) - 1]);
        for (let i = 0; i < featureCount; i++) {
            masks.add(1 << i);
            for (let j = i + 1; j < featureCount; j++) {
                masks.add((1 << i) | (1 << j));
            }
        }
        for (const mask of masks) {
            assert(await page.evaluate((mask) => {
                const choices = [...document.querySelectorAll(".quick-start-features input")];
                choices.forEach((input, index) => {
                    input.checked = Boolean(mask & (1 << index));

                });
                choices[0].dispatchEvent(new Event("change", {bubbles: true}));
                return [...document.querySelectorAll("[data-quick-feature]")].every((block) =>
                    block.hidden !== choices.find((input) => input.value === block.dataset.quickFeature).checked);
            }, mask));
        }
        assert.deepEqual(errors, [], "Documentation scripts must run without browser errors");
        await page.setJavaScriptEnabled(false);
        await page.reload({waitUntil: "networkidle2"});
        assert.equal(await page.$$eval("[data-quick-feature]",(els)=>els.filter((el)=>getComputedStyle(el).display!=="none").length),23);
        console.log(JSON.stringify({passed:"8 steps, feature selection, URL restoration, browser back, deep links, print, mobile overflow, no-JS, all individual features, pairs and full selection",errors}));
    } finally {
        await browser?.close();
        server.close();
    }
})();

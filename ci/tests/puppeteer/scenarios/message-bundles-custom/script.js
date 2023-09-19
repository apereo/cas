const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const propertiesReader = require('properties-reader');
const path = require("path");

function updateProperty(properties, propertiesFile, value) {
    properties.set("screen.welcome.security", value);
    properties.save(propertiesFile, (err, data) => {
        if (err) {
            throw err;
        }
    });
}

(async () => {
    let browser = await puppeteer.launch(cas.browserOptions());
    let propertiesFile = path.join(__dirname, 'custom_messages.properties');
    let properties = propertiesReader(propertiesFile, 'utf-8', {writer: { saveSections: false }});
    try {
        await cas.log(`Loading properties file ${propertiesFile}`);
        const page = await cas.newPage(browser);
        await cas.goto(page, "https://localhost:8443/cas/login");
        await page.waitForTimeout(1000);
        await cas.assertInnerText(page, "#sidebar div p", "Stay safe!");
        await cas.assertInnerText(page, "#login-form-controls h3 span", "Welcome to CAS");

        updateProperty(properties, propertiesFile, "Hello World!");

        await page.waitForTimeout(2000);
        await page.reload("https://localhost:8443/cas/login");
        await cas.assertInnerText(page, "#sidebar div p", "Hello World!");
    } finally {
        updateProperty(properties, propertiesFile, "Stay safe!");
        await browser.close();
    }
})();

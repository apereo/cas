
const cas = require("../../cas.js");
const propertiesReader = require("properties-reader");
const path = require("path");

async function updateProperty(properties, propertiesFile, value) {
    await properties.set("screen.welcome.security", value);
    await properties.save(propertiesFile, (err, data) => {
        cas.logb(`Saving ${data}`);
        if (err) {
            throw err;
        }
    });
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const propertiesFile = path.join(__dirname, "custom_messages.properties");
    const properties = propertiesReader(propertiesFile, "utf-8", {writer: { saveSections: false }});
    try {
        await cas.log(`Loading properties file ${propertiesFile}`);
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#sidebar div p", "Stay safe!");
        await cas.assertInnerText(page, "#login-form-controls h2 span", "Welcome to CAS");

        await updateProperty(properties, propertiesFile, "Hello World!");

        await cas.sleep(2000);
        await page.reload("https://localhost:8443/cas/login");
        await cas.assertInnerText(page, "#sidebar div p", "Hello World!");
    } finally {
        await updateProperty(properties, propertiesFile, "Stay safe!");
        await cas.closeBrowser(browser);
    }
})();

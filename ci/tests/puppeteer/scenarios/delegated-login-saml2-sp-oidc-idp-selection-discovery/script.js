
const cas = require("../../cas.js");
const path = require("path");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await startWithSamlSp(page);
    await startWithCasSp(page);
    await browser.close();
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
})();

async function startWithCasSp(page) {
    await cas.log("Starting with CAS SP");
    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogout(page);
    await cas.sleep(1000);
    await cas.gotoLogin(page, service);
    await cas.assertVisibility(page, "#selectProviderButton");
    await cas.submitForm(page, "#providerDiscoveryForm");
    await cas.sleep(4000);
    await cas.type(page, "#username", "casuser@heroku.org");
    await cas.submitForm(page, "#discoverySelectionForm");
    await cas.sleep(7000);
    await cas.loginWith(page);
    await cas.sleep(7000);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.validateTicket(service, ticket, "XML");
    assert(body.includes("<cas:user>casuser</cas:user>"));
}

async function startWithSamlSp(page) {
    await cas.log("Starting with SAML SP");
    await cas.gotoLogout(page);

    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(4000);
    
    await cas.assertVisibility(page, "#selectProviderButton");
    await cas.submitForm(page, "#providerDiscoveryForm");
    await cas.sleep(5000);
    await cas.type(page, "#username", "casuser@example.org");
    await cas.submitForm(page, "#discoverySelectionForm");
    await cas.sleep(4000);
    await cas.loginWith(page, "info@fawnoos.com", "QFkN&d^bf9vhS3KS49",
        "#okta-signin-username", "#okta-signin-password");

    await cas.sleep(6000);
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");
    const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);

    await cas.gotoLogin(page);
    await cas.sleep(2000);
    await cas.assertCookie(page);
}

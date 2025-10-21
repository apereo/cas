
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.updateYamlConfigurationSource(__dirname, {
            cas: {
                authn: {
                    syncope: {
                        url: "http://localhost:18080/syncope"
                    }
                }
            }
        });
        await cas.sleep(3000);
        await cas.refreshContext();
        await doLogin(page, "syncopecas", "Mellon", "syncopecas@syncope.org");
        await doLogin(page, "casuser", "paSSw0rd", "casuser@syncope.org");
    } finally {
        await cas.updateYamlConfigurationSource(__dirname, {
            cas: {
                authn: {
                    syncope: {
                        url: ""
                    }
                }
            }
        });
    }
    await cas.closeBrowser(browser);
})();

async function doLogin(page, uid, psw, email) {
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page,uid, psw);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.sleep(1000);
    const attributes = await cas.innerText(page, "#attribute-tab-0 table#attributesTable tbody");
    assert(attributes.includes("syncopeUserAttr_email"));
    assert(attributes.includes(email));
}


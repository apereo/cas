const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl, (res) => {
        assert(res.status === 200);
        const entries = res.data[1];
        const length = entries.length;
        cas.log(`Services found: ${length}`);
        assert(length === 2);

        assert(entries[0].id === 1);
        assert(entries[0].name === "Sample");

        assert(entries[1].id === 2);
        assert(entries[1].name === "Fancy");
    }, (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });
})();

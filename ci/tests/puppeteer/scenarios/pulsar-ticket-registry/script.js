const cas = require("../../cas.js");

(async () => {
    await cas.verifyTicketRegistryAcrossNodes();
})();

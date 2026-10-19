const express = require("express");

/*
 * A REST configuration backend for CAS, started before CAS boots. Properties live in memory:
 * GET /config/names lists the names, GET /config?name= returns a value as plain text,
 * POST /config?name=&value= sets one, DELETE /config?name= removes one and DELETE /config removes all.
 */
const properties = new Map([
    ["cas.authn.accept.users", "restuser::Mellon"],
    ["cas.server.scope", "example.org"]
]);

const app = express();
app.get("/config/names", (req, res) => res.json([...properties.keys()]));
app.get("/config", (req, res) => {
    const value = properties.get(req.query.name);
    if (value === undefined) {
        res.sendStatus(404);
    } else {
        res.type("text/plain").send(value);
    }
});
app.post("/config", (req, res) => {
    console.log(`Setting ${req.query.name}=${req.query.value}`);
    properties.set(req.query.name, req.query.value);
    res.sendStatus(200);
});
app.delete("/config", (req, res) => {
    if (req.query.name === undefined) {
        properties.clear();
    } else {
        properties.delete(req.query.name);
    }
    res.sendStatus(200);
});
app.listen(5618, () => console.log("REST configuration backend is listening on port 5618"));

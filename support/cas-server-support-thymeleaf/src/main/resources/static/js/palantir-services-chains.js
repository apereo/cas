/**
 * Chained policies in the application wizard.
 *
 * A setting that CAS can chain, such as the access strategy or the attribute release policy, gets a bar above its
 * type: a switch that makes it a chain, how the chain combines its members, and a tab per member. The section's usual
 * fields edit the member whose tab is selected, so every member gets the full form of its type.
 *
 * The wizard's fields only ever hold one member. Before the wizard is filled from a definition, each chain is taken
 * apart, outer chains first, and the selected member put in its place; when the wizard generates the definition, each
 * chain is put back together, inner chains first, with the member read from the fields. Moving to another member
 * generates the definition and fills the wizard again with that member selected.
 *
 * A member the wizard has no type for, such as a chain inside a chain, is kept as it is and edited as JSON.
 */
class PalantirWizardChains {

    static LIST = "java.util.ArrayList";

    static OPERATORS = {
        allow: [
            {value: "AND", text: "Every One Must Allow"},
            {value: "OR", text: "Any One May Allow"}
        ],
        hold: [
            {value: "AND", text: "Every One Must Hold"},
            {value: "OR", text: "Any One May Hold"}
        ],
        merge: [
            {value: "REPLACE", text: "Later Ones Replace"},
            {value: "ADD", text: "Earlier Ones Win"},
            {value: "MULTIVALUED", text: "Values Add Up"}
        ]
    };

    static KINDS = [
        {
            path: "accessStrategy",
            section: "editServiceWizardMenuItemAccessStrategy",
            chain: "org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy",
            members: "strategies",
            combine: {key: "operator", choices: "allow", initial: "AND"},
            one: "strategy",
            many: "strategies",
            initial: "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy"
        },
        {
            path: "accessStrategy.activationCriteria",
            section: "editServiceWizardMenuItemAccessStrategyActivation",
            chain: "org.apereo.cas.services.ChainingRegisteredServiceAccessStrategyActivationCriteria",
            members: "conditions",
            combine: {key: "operator", choices: "hold", initial: "AND"},
            one: "condition",
            many: "conditions",
            initial: "org.apereo.cas.services.AttributeBasedRegisteredServiceAccessStrategyActivationCriteria"
        },
        {
            path: "accessStrategy.delegatedAuthenticationPolicy",
            section: "editServiceWizardMenuItemDelegatedAuthenticationPolicy",
            chain: "org.apereo.cas.services.ChainingRegisteredServiceDelegatedAuthenticationPolicy",
            members: "strategies",
            combine: {key: "operator", choices: "allow", initial: "AND"},
            one: "policy",
            many: "policies",
            initial: "org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy"
        },
        {
            path: "attributeReleasePolicy",
            section: "editServiceWizardMenuItemAttributeReleasePolicy",
            chain: "org.apereo.cas.services.ChainingAttributeReleasePolicy",
            members: "policies",
            combine: {key: "mergingPolicy", choices: "merge", initial: "REPLACE"},
            one: "policy",
            many: "policies",
            initial: "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy"
        },
        {
            path: "attributeReleasePolicy.activationCriteria",
            section: "editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria",
            chain: "org.apereo.cas.services.ChainingRegisteredServiceAttributeReleaseActivationCriteria",
            members: "conditions",
            combine: {key: "operator", choices: "hold", initial: "AND"},
            one: "condition",
            many: "conditions",
            initial: "org.apereo.cas.services.AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria"
        },
        {
            path: "attributeReleasePolicy.attributeFilter",
            section: "editServiceWizardMenuItemAttributeReleaseValueFilters",
            chain: "org.apereo.cas.services.support.RegisteredServiceChainingAttributeFilter",
            members: "filters",
            hint: "Each filter runs on the attributes and what they keep is combined.",
            one: "filter",
            many: "filters",
            initial: "org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter"
        },
        {
            path: "attributeReleasePolicy.consentPolicy",
            section: "editServiceWizardMenuItemAttributeReleaseValueConsentPolicy",
            chain: "org.apereo.cas.services.consent.ChainingRegisteredServiceConsentPolicy",
            members: "policies",
            hint: "Their excluded and included attributes and services are combined.",
            one: "policy",
            many: "policies",
            initial: "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy"
        },
        {
            path: "singleSignOnParticipationPolicy",
            section: "editServiceWizardMenuItemSSOParticipationPolicy",
            chain: "org.apereo.cas.services.ChainingRegisteredServiceSingleSignOnParticipationPolicy",
            members: "policies",
            hint: "Single sign-on is honored only when every policy agrees.",
            one: "policy",
            many: "policies",
            initial: "org.apereo.cas.services.LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy"
        }
    ];

    static state = new Map();

    static preferred = new Map();

    static captured = new Map();

    static signatures = new Map();

    static SMALL_WORDS = new Set(["a", "an", "the", "and", "or", "of", "in", "on", "to", "for", "by", "with", "at", "as"]);

    /**
     * What the wizard models, kept while its fields stay the same: reading it walks every field of the wizard.
     */
    static model = null;

    /**
     * Forget every chain, as when the wizard opens.
     */
    static reset() {
        PalantirWizardChains.state.clear();
        PalantirWizardChains.preferred.clear();
        PalantirWizardChains.captured.clear();
        PalantirWizardChains.signatures.clear();
        PalantirWizardChains.model = null;
        $("#editServiceWizardForm .palantir-chain").remove();
    }

    static kind(path) {
        return PalantirWizardChains.KINDS.find(kind => kind.path === path);
    }

    static depth(path) {
        return path.split(".").length;
    }

    static read(definition, path) {
        return path.split(".").reduce((current, part) => current === null || current === undefined ? undefined : current[part], definition);
    }

    static write(definition, path, value) {
        const parts = path.split(".");
        const parent = parts.slice(0, -1).reduce((current, part) => current === null || current === undefined ? undefined : current[part], definition);
        if (parent === null || typeof parent !== "object") {
            return;
        }
        if (value === undefined) {
            delete parent[parts.at(-1)];
        } else {
            parent[parts.at(-1)] = value;
        }
    }

    static list(value) {
        if (Array.isArray(value)) {
            return value.length === 2 && typeof value[0] === "string" && Array.isArray(value[1]) ? value[1] : value;
        }
        return [];
    }

    /**
     * The type of what holds a setting: the policy a nested setting belongs to, or the application for a top one. A
     * nested chain only lasts while that type does, the way the fields of a nested setting are cleared when the type
     * of the policy they belong to changes.
     *
     * @param definition the definition
     * @param path the setting
     * @returns {string} the type
     */
    static holder(definition, path) {
        const parent = path.includes(".") ? PalantirWizardChains.read(definition, path.slice(0, path.lastIndexOf("."))) : definition;
        return String(parent?.["@class"] ?? "");
    }

    static clone(value) {
        return value === undefined ? undefined : structuredClone(value);
    }

    static select(kind) {
        return $(`#editServiceWizardForm select[data-param-name="${kind.path}"]`).first();
    }

    /**
     * The types the section of a chain offers, with their names.
     *
     * @param kind the kind of chain
     * @returns {Map<string, string>} the names of the types, by type
     */
    static options(kind) {
        const select = PalantirWizardChains.select(kind)[0];
        return new Map(select ? Array.from(select.options, option => [option.value, option.textContent.trim()]) : []);
    }

    /**
     * Whether the wizard has fields for a member: its type is one of the types the section offers.
     *
     * @param kind the kind of chain
     * @param member the member
     * @param options the types the section offers, when already read
     * @returns {boolean} true when the section's fields can edit it
     */
    static editable(kind, member, options = PalantirWizardChains.options(kind)) {
        const type = member?.["@class"];
        return Boolean(type) && options.has(type);
    }

    /**
     * Title case for labels: every word starts with a capital letter but the short words in between.
     *
     * @param text the text
     * @returns {string} the text in title case
     */
    static capitalize(text) {
        const words = String(text).split(" ");
        return words.map((word, index) => index > 0 && index < words.length - 1 && PalantirWizardChains.SMALL_WORDS.has(word.toLowerCase())
            ? word.toLowerCase()
            : word.charAt(0).toUpperCase() + word.slice(1)).join(" ");
    }

    static label(kind, member, options = PalantirWizardChains.options(kind)) {
        const type = String(member?.["@class"] ?? "");
        if (type === "") {
            return "Not Chosen";
        }
        const chained = PalantirWizardChains.KINDS.find(entry => entry.chain === type);
        if (chained) {
            return `Chain of ${PalantirWizardChains.list(member[chained.members]).length}`;
        }
        const text = options.has(type) ? options.get(type) : type.split(".").pop().replace(/RegisteredService|AttributeReleasePolicy|Policy$/g, " ");
        return PalantirWizardChains.capitalize(text.replace(/([a-z])([A-Z])/g, "$1 $2").trim().toLowerCase());
    }

    /**
     * Take the chains of a definition apart before the wizard is filled from it: each chain is remembered and its
     * selected member put in its place, outer chains first, so a chain inside a member is found in that member.
     *
     * @param definition the definition
     * @returns {object} a copy of the definition with one member in place of each chain
     */
    static unpack(definition) {
        PalantirWizardChains.state.clear();
        PalantirWizardChains.captured.clear();
        if (definition === null || typeof definition !== "object") {
            return definition;
        }
        const copy = structuredClone(definition);
        [...PalantirWizardChains.KINDS]
            .sort((left, right) => PalantirWizardChains.depth(left.path) - PalantirWizardChains.depth(right.path))
            .forEach(kind => {
                const node = PalantirWizardChains.read(copy, kind.path);
                if (node === null || typeof node !== "object" || node["@class"] !== kind.chain) {
                    return;
                }
                const members = PalantirWizardChains.list(node[kind.members]).map(member => structuredClone(member));
                const extras = Object.fromEntries(Object.entries(node).filter(([key]) => key !== "@class" && key !== kind.members));
                if (kind.combine && extras[kind.combine.key] === undefined) {
                    extras[kind.combine.key] = kind.combine.initial;
                }
                const wanted = PalantirWizardChains.preferred.get(kind.path) ?? 0;
                const options = PalantirWizardChains.options(kind);
                const candidates = members.map((member, index) => index).filter(index => PalantirWizardChains.editable(kind, members[index], options));
                const active = candidates.includes(wanted) ? wanted : candidates[0] ?? -1;
                PalantirWizardChains.state.set(kind.path, {members, base: members.map(member => structuredClone(member)), active, extras,
                    holder: PalantirWizardChains.holder(copy, kind.path)});
                PalantirWizardChains.preferred.set(kind.path, active);
                PalantirWizardChains.write(copy, kind.path, active >= 0 ? structuredClone(members[active]) : undefined);
            });
        return copy;
    }

    /**
     * Put the chains of a generated definition back together, inner chains first: the member the fields hold goes
     * back in its place and the definition gets the chain.
     *
     * @param definition the definition generated from the fields
     */
    static pack(definition) {
        [...PalantirWizardChains.KINDS]
            .sort((left, right) => PalantirWizardChains.depth(right.path) - PalantirWizardChains.depth(left.path))
            .forEach(kind => {
                const node = PalantirWizardChains.read(definition, kind.path);
                PalantirWizardChains.captured.set(kind.path, PalantirWizardChains.clone(node));
                PalantirWizardChains.captured.set(`holder:${kind.path}`, PalantirWizardChains.holder(definition, kind.path));
                const chain = PalantirWizardChains.state.get(kind.path);
                if (!chain) {
                    return;
                }
                if (chain.holder !== PalantirWizardChains.holder(definition, kind.path)) {
                    PalantirWizardChains.state.delete(kind.path);
                    PalantirWizardChains.preferred.delete(kind.path);
                    return;
                }
                if (chain.active >= 0) {
                    chain.members[chain.active] = node !== null && typeof node === "object" && node["@class"]
                        ? PalantirWizardChains.keep(kind, chain.base[chain.active], structuredClone(node))
                        : null;
                }
                const members = chain.members.filter(member => member !== null && member !== undefined);
                PalantirWizardChains.write(definition, kind.path, {
                    "@class": kind.chain,
                    ...structuredClone(chain.extras),
                    [kind.members]: [PalantirWizardChains.LIST, members]
                });
            });
    }

    /**
     * The member the fields generated, with the settings of the member it was read from that the wizard has no field
     * for, the way going back to the JSON editor keeps them for a policy that is not chained.
     *
     * @param kind the kind of chain
     * @param base the member as the definition held it
     * @param node the member the fields generated
     * @returns {object} the member
     */
    static keep(kind, base, node) {
        if (base === null || base === undefined || base["@class"] !== node["@class"] || typeof PalantirServiceEditor === "undefined") {
            return node;
        }
        const wizard = PalantirWizardChains.modeled();
        const prefix = `${kind.path}.`;
        const inside = paths => paths.filter(path => path.startsWith(prefix)).map(path => path.slice(prefix.length));
        const offered = Object.fromEntries(Object.entries(wizard.offered)
            .filter(([path]) => path.startsWith(prefix))
            .map(([path, choices]) => [path.slice(prefix.length), choices]));
        try {
            return JSON.parse(PalantirServiceEditor.carry(base, JSON.stringify(node), inside(wizard.modeled), inside(wizard.maps), offered));
        } catch (error) {
            return node;
        }
    }

    /**
     * What the wizard models, read again only when its fields or the type of application change.
     *
     * @returns {{modeled: string[], maps: string[], offered: Object<string, string[]>}} the settings, maps and choices
     */
    static modeled() {
        const form = document.getElementById("editServiceWizardForm");
        const key = `${$(form).data("service-class-name")}:${form?.querySelectorAll("[data-param-name]").length ?? 0}`;
        if (PalantirWizardChains.model?.key !== key) {
            PalantirWizardChains.model = {
                key,
                modeled: PalantirServiceEditor.wizardSettings(),
                maps: PalantirServiceEditor.wizardMaps(),
                offered: PalantirServiceEditor.wizardChoices()
            };
        }
        return PalantirWizardChains.model;
    }

    /**
     * Read the fields into the chains and return the definition they make, before changing a chain.
     *
     * @returns {object|null} the definition
     */
    static sync() {
        return buildWizardServiceDefinition();
    }

    /**
     * Fill the wizard again from a definition, keeping the place the operator is at.
     *
     * @param definition the definition
     * @param focus the path of the chain whose selected tab gets the focus afterwards
     */
    static refill(definition, focus) {
        const pane = $("#editServiceWizardDialog");
        const scroll = pane.scrollTop();
        resetWizardSwitches();
        $("#editServiceWizardForm [id$='MapContainerToAppend']").empty();
        $("#editServiceWizardForm input").val("").removeAttr("data-mapped-value-type data-mapped-value-key");
        if (typeof resetRegisteredServiceClientSecrets === "function") {
            resetRegisteredServiceClientSecrets();
        }
        $("#editServiceWizardForm .jqueryui-multiselectmenu").each(function () {
            this.tomselect?.clear();
        });
        setDescriptionEditorValue("");
        resetWizardSelects();
        populateWizardFromServiceDefinition(definition ?? {"@class": $("#serviceClassType").text().trim()}, {
            focus: false,
            done: () => {
                pane.scrollTop(scroll);
                if (focus) {
                    $(`#editServiceWizardForm .palantir-chain[data-chain-path="${focus}"] .palantir-chain__tab.is-active`).trigger("focus");
                }
            }
        });
    }

    /**
     * Read the fields into the chains, then the chain of a setting as it stands: a nested chain ends when the policy
     * holding it changed type, and then there is nothing left to change.
     *
     * @param path the setting
     * @returns {object|null} the chain
     */
    static current(path) {
        PalantirWizardChains.sync();
        const chain = PalantirWizardChains.state.get(path) ?? null;
        if (chain === null) {
            PalantirWizardChains.render(true);
        }
        return chain;
    }

    static forgetInner(path) {
        [...PalantirWizardChains.preferred.keys()]
            .filter(key => key.startsWith(`${path}.`))
            .forEach(key => PalantirWizardChains.preferred.delete(key));
    }

    /**
     * Make a setting a chain of the one policy it has, or a single policy again.
     *
     * @param path the setting
     * @param on whether it becomes a chain
     */
    static async toggle(path, on) {
        const kind = PalantirWizardChains.kind(path);
        PalantirWizardChains.sync();
        if (on) {
            const current = PalantirWizardChains.captured.get(path);
            const member = current?.["@class"] ? current : {"@class": PalantirWizardChains.initialType(kind)};
            const extras = kind.combine ? {[kind.combine.key]: kind.combine.initial} : {};
            const holder = PalantirWizardChains.captured.get(`holder:${path}`) ?? "";
            PalantirWizardChains.state.set(path, {members: [member], base: [structuredClone(member)], active: 0, extras, holder});
            PalantirWizardChains.preferred.set(path, 0);
            PalantirWizardChains.finish(path, !current?.["@class"]);
            return;
        }
        const chain = PalantirWizardChains.state.get(path);
        const kept = chain?.members.filter(Boolean).length ?? 0;
        if (kept > 1) {
            const answer = await Swal.fire({
                icon: "question",
                titleText: `Keep Only ${PalantirWizardChains.label(kind, chain.members[chain.active])}?`,
                text: `The other ${kept - 1} ${kept - 1 === 1 ? kind.one : kind.many} of the chain are removed.`,
                showCancelButton: true,
                confirmButtonText: "Keep One"
            });
            if (!answer.isConfirmed) {
                PalantirWizardChains.render(true);
                return;
            }
        }
        PalantirWizardChains.state.delete(path);
        PalantirWizardChains.preferred.delete(path);
        PalantirWizardChains.finish(path, false);
    }

    static initialType(kind) {
        const options = PalantirWizardChains.select(kind).find("option").toArray().map(option => option.value).filter(Boolean);
        return options.includes(kind.initial) ? kind.initial : options[0] ?? kind.initial;
    }

    /**
     * Show another member in the fields.
     *
     * @param path the setting
     * @param index the member
     */
    static choose(path, index) {
        const kind = PalantirWizardChains.kind(path);
        const definition = PalantirWizardChains.sync();
        const chain = PalantirWizardChains.state.get(path);
        if (!chain) {
            PalantirWizardChains.render(true);
            return;
        }
        if (index === chain.active) {
            return;
        }
        if (!PalantirWizardChains.editable(kind, chain.members[index])) {
            PalantirWizardChains.editAsJson(path, index);
            return;
        }
        PalantirWizardChains.preferred.set(path, index);
        PalantirWizardChains.forgetInner(path);
        PalantirWizardChains.refill(definition, path);
    }

    static add(path) {
        const kind = PalantirWizardChains.kind(path);
        const chain = PalantirWizardChains.current(path);
        if (!chain) {
            return;
        }
        chain.members.push({"@class": PalantirWizardChains.initialType(kind)});
        chain.base.push(null);
        PalantirWizardChains.preferred.set(path, chain.members.length - 1);
        PalantirWizardChains.forgetInner(path);
        PalantirWizardChains.refill(PalantirWizardChains.sync(), path);
    }

    static remove(path, index) {
        const chain = PalantirWizardChains.current(path);
        if (!chain || chain.members.length < 2) {
            return;
        }
        const wasActive = index === chain.active;
        chain.members.splice(index, 1);
        chain.base.splice(index, 1);
        if (wasActive) {
            chain.active = -1;
            PalantirWizardChains.preferred.set(path, Math.max(0, index - 1));
            PalantirWizardChains.forgetInner(path);
            PalantirWizardChains.refill(PalantirWizardChains.sync(), path);
            return;
        }
        if (index < chain.active) {
            chain.active--;
        }
        PalantirWizardChains.preferred.set(path, chain.active);
        PalantirWizardChains.finish(path, false);
    }

    /**
     * Move a member one place earlier or later. Members that carry an order are numbered again in their new order,
     * since CAS sorts some chains by it.
     *
     * @param path the setting
     * @param index the member
     * @param step -1 to move it earlier, 1 to move it later
     */
    static move(path, index, step) {
        const chain = PalantirWizardChains.current(path);
        const target = index + step;
        if (!chain || target < 0 || target >= chain.members.length) {
            return;
        }
        const [member] = chain.members.splice(index, 1);
        chain.members.splice(target, 0, member);
        const [base] = chain.base.splice(index, 1);
        chain.base.splice(target, 0, base);
        if (chain.members.some(entry => typeof entry?.order === "number")) {
            [chain.members, chain.base].forEach(list => list.forEach((entry, position) => {
                if (entry !== null && typeof entry === "object") {
                    entry.order = position;
                }
            }));
        }
        const active = chain.active === index ? target : chain.active === target ? index : chain.active;
        chain.active = -1;
        PalantirWizardChains.preferred.set(path, active);
        PalantirWizardChains.refill(PalantirWizardChains.sync(), path);
    }

    static combine(path, value) {
        const kind = PalantirWizardChains.kind(path);
        const chain = PalantirWizardChains.current(path);
        if (!chain || !kind.combine) {
            return;
        }
        chain.extras[kind.combine.key] = value;
        PalantirWizardChains.finish(path, false);
    }

    /**
     * Edit, as JSON, a member the wizard has no fields for.
     *
     * @param path the setting
     * @param index the member
     */
    static async editAsJson(path, index) {
        const kind = PalantirWizardChains.kind(path);
        const chain = PalantirWizardChains.state.get(path);
        const member = chain.members[index];
        const answer = await Swal.fire({
            titleText: `${PalantirWizardChains.label(kind, member)} · ${PalantirWizardChains.capitalize(kind.one)} ${index + 1}`,
            html: `<p class="palantir-chain__json-note">The wizard has no fields for this ${escapePalantirMarkup(kind.one)}, so it is edited as JSON.</p>`,
            input: "textarea",
            inputValue: JSON.stringify(member, null, 2),
            inputAttributes: {"aria-label": `${kind.one} ${index + 1} as JSON`, spellcheck: "false"},
            customClass: {input: "palantir-chain__json"},
            showCancelButton: true,
            confirmButtonText: "Keep",
            preConfirm: text => {
                try {
                    const parsed = JSON.parse(text);
                    if (parsed === null || typeof parsed !== "object" || Array.isArray(parsed) || !parsed["@class"]) {
                        Swal.showValidationMessage("Enter one JSON object with its @class.");
                        return false;
                    }
                    return parsed;
                } catch (error) {
                    Swal.showValidationMessage(`This is not JSON: ${error.message}`);
                    return false;
                }
            }
        });
        if (!answer.isConfirmed) {
            return;
        }
        const latest = PalantirWizardChains.current(path);
        if (latest !== chain || index >= chain.members.length) {
            return;
        }
        chain.members[index] = answer.value;
        chain.base[index] = structuredClone(answer.value);
        if (PalantirWizardChains.editable(kind, answer.value) && chain.active < 0) {
            PalantirWizardChains.preferred.set(path, index);
            PalantirWizardChains.refill(PalantirWizardChains.sync(), path);
            return;
        }
        PalantirWizardChains.finish(path, false);
    }

    static finish(path, refill) {
        if (refill) {
            PalantirWizardChains.refill(PalantirWizardChains.sync(), path);
            return;
        }
        generateServiceDefinition();
        PalantirWizardChains.render(true);
    }

    /**
     * Draw the chain bar of every setting that can be chained, above its type.
     *
     * @param force whether to draw even when nothing it shows has changed
     */
    static render(force = false) {
        const escape = escapePalantirMarkup;
        PalantirWizardChains.KINDS.forEach(kind => {
            const select = PalantirWizardChains.select(kind);
            const anchor = select.closest("[id$='SelectContainer']");
            if (select.length === 0 || anchor.length === 0) {
                return;
            }
            const chain = PalantirWizardChains.state.get(kind.path);
            const options = chain ? PalantirWizardChains.options(kind) : null;
            const signature = JSON.stringify(chain
                ? {active: chain.active, extras: chain.extras, labels: chain.members.map(member => PalantirWizardChains.label(kind, member, options)),
                    editable: chain.members.map(member => PalantirWizardChains.editable(kind, member, options))}
                : null);
            let bar = $(`#editServiceWizardForm .palantir-chain[data-chain-path="${kind.path}"]`);
            if (!force && bar.length > 0 && PalantirWizardChains.signatures.get(kind.path) === signature) {
                return;
            }
            PalantirWizardChains.signatures.set(kind.path, signature);
            if (bar.length === 0) {
                bar = $(`<div class="palantir-chain" data-chain-path="${escape(kind.path)}"></div>`)
                    .toggleClass("advanced-option", anchor.hasClass("advanced-option"))
                    .insertBefore(anchor);
            }
            const id = `chain${kind.path.replace(/\W+(\w)/g, (match, letter) => letter.toUpperCase()).replace(/^\w/, letter => letter.toUpperCase())}`;
            const switchMarkup = `
                <label class="palantir-feature-switch palantir-chain__switch" for="${id}Switch">
                    <input type="checkbox" role="switch" id="${id}Switch" data-chain-action="toggle"${chain ? " checked" : ""}${chain && chain.active < 0
                        ? ` disabled title="${escape(`Edit the ${kind.many} as JSON, or remove all but one, to make this a single ${kind.one} again`)}"` : ""}/>
                    <span class="palantir-feature-switch__track" aria-hidden="true"></span>
                    <span class="palantir-feature-switch__text">
                        <strong>Chain Several ${escape(PalantirWizardChains.capitalize(kind.many))}</strong>
                        <small>${chain ? escape(kind.hint ?? "Pick how the chain decides, then edit each one in its tab.")
                            : escape(`Combine ${kind.many} that each check something different.`)}</small>
                    </span>
                </label>`;
            if (!chain) {
                bar.removeClass("is-on").html(switchMarkup);
                return;
            }
            const combine = kind.combine
                ? `<div class="palantir-chain__combine" role="radiogroup" aria-label="How the chain decides">
                    ${PalantirWizardChains.choices(kind, chain).map(choice => `
                        <button type="button" role="radio" class="palantir-chain__choice${chain.extras[kind.combine.key] === choice.value ? " is-active" : ""}"
                                aria-checked="${chain.extras[kind.combine.key] === choice.value}" data-chain-action="combine"
                                data-chain-value="${escape(choice.value)}">${escape(choice.text)}</button>`).join("")}
                   </div>`
                : "";
            const tabs = chain.members.map((member, index) => {
                const editable = PalantirWizardChains.editable(kind, member, options);
                const active = index === chain.active;
                return `
                    <button type="button" role="tab" class="palantir-chain__tab${active ? " is-active" : ""}${editable ? "" : " is-json"}"
                            aria-selected="${active}" tabindex="${active || (chain.active < 0 && index === 0) ? 0 : -1}"
                            data-chain-action="choose" data-chain-index="${index}"
                            title="${escape(editable ? `Edit ${kind.one} ${index + 1}` : `Edit ${kind.one} ${index + 1} as JSON`)}">
                        <span class="palantir-chain__number">${index + 1}</span>
                        <span class="palantir-chain__name">${escape(PalantirWizardChains.label(kind, member, options))}</span>
                        ${editable ? "" : `<i class="mdi mdi-code-json" aria-hidden="true"></i>`}
                    </button>`;
            }).join("");
            const current = chain.active >= 0 ? chain.members[chain.active] : null;
            const header = current
                ? `<div class="palantir-chain__member">
                    <span><strong>${escape(PalantirWizardChains.capitalize(kind.one))} ${chain.active + 1} of ${chain.members.length}</strong>
                        · ${escape(PalantirWizardChains.label(kind, current, options))}</span>
                    <span class="palantir-chain__member-actions">
                        <button type="button" class="palantir-icon-button" data-chain-action="move" data-chain-step="-1"
                                title="Move earlier" aria-label="Move ${escape(kind.one)} ${chain.active + 1} earlier"${chain.active === 0 ? " disabled" : ""}>
                            <i class="mdi mdi-arrow-left" aria-hidden="true"></i></button>
                        <button type="button" class="palantir-icon-button" data-chain-action="move" data-chain-step="1"
                                title="Move later" aria-label="Move ${escape(kind.one)} ${chain.active + 1} later"${chain.active === chain.members.length - 1 ? " disabled" : ""}>
                            <i class="mdi mdi-arrow-right" aria-hidden="true"></i></button>
                        <button type="button" class="palantir-icon-button" data-chain-action="remove"
                                title="Remove from the chain" aria-label="Remove ${escape(kind.one)} ${chain.active + 1}"${chain.members.length < 2 ? " disabled" : ""}>
                            <i class="mdi mdi-delete-outline" aria-hidden="true"></i></button>
                    </span>
                   </div>`
                : `<p class="palantir-chain__member is-json">Every ${escape(kind.one)} of this chain is edited as JSON: choose one above.</p>`;
            bar.addClass("is-on").html(`
                ${switchMarkup}
                ${combine}
                <div class="palantir-chain__tabs" role="tablist" aria-label="${escape(kind.many)} of the chain">
                    ${tabs}
                    <button type="button" class="palantir-chain__add" data-chain-action="add" title="Add a ${escape(kind.one)}"
                            aria-label="Add a ${escape(kind.one)} to the chain"><i class="mdi mdi-plus" aria-hidden="true"></i></button>
                </div>
                ${header}`);
        });
    }

    static choices(kind, chain) {
        const choices = [...PalantirWizardChains.OPERATORS[kind.combine.choices]];
        const value = chain.extras[kind.combine.key];
        if (value && !choices.some(choice => choice.value === value)) {
            choices.push({value, text: String(value).charAt(0) + String(value).slice(1).toLowerCase()});
        }
        return choices;
    }

    /**
     * Answer the bars' buttons, switches and keys.
     */
    static bind() {
        $(document)
            .off(".palantirChain")
            .on("change.palantirChain", "#editServiceWizardForm .palantir-chain [data-chain-action='toggle']", event => {
                if (isPopulatingWizard) {
                    event.currentTarget.checked = !event.currentTarget.checked;
                    return;
                }
                const path = $(event.currentTarget).closest(".palantir-chain").data("chain-path");
                PalantirWizardChains.toggle(path, event.currentTarget.checked);
            })
            .on("click.palantirChain", "#editServiceWizardForm .palantir-chain button[data-chain-action]", event => {
                event.preventDefault();
                if (isPopulatingWizard) {
                    return;
                }
                const button = event.currentTarget;
                const path = $(button).closest(".palantir-chain").data("chain-path");
                const chain = PalantirWizardChains.state.get(path);
                const action = button.dataset.chainAction;
                if (action === "choose") {
                    PalantirWizardChains.choose(path, Number(button.dataset.chainIndex));
                } else if (action === "add") {
                    PalantirWizardChains.add(path);
                } else if (action === "remove" && chain) {
                    PalantirWizardChains.remove(path, chain.active);
                } else if (action === "move" && chain) {
                    PalantirWizardChains.move(path, chain.active, Number(button.dataset.chainStep));
                } else if (action === "combine") {
                    PalantirWizardChains.combine(path, button.dataset.chainValue);
                }
            })
            .on("keydown.palantirChain", "#editServiceWizardForm .palantir-chain__tab", event => {
                if (event.key !== "ArrowLeft" && event.key !== "ArrowRight") {
                    return;
                }
                const tabs = $(event.currentTarget).parent().children(".palantir-chain__tab");
                const index = tabs.index(event.currentTarget) + (event.key === "ArrowRight" ? 1 : -1);
                if (index >= 0 && index < tabs.length) {
                    event.preventDefault();
                    tabs.eq(index).trigger("focus");
                }
            });
    }
}

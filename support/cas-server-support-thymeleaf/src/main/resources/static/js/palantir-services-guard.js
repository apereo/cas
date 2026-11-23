/**
 * Keeps edits in the application wizard and the plain definition editor from being lost.
 *
 * Each dialog has a mode, wizard or editor, whose state remembers the definition as the dialog first settled.
 * Once the definition differs from it, closing the dialog asks whether to keep editing, keep a draft and close,
 * or discard the changes, and a draft is kept in this browser's storage while the edits stay unsaved, so a page
 * reload or a closed tab can be picked up again. Reopening the same application, or a new application of the
 * same type, offers to resume the draft. Saving or discarding removes it, and a draft older than thirty days is
 * dropped. Nothing is sent to the server.
 *
 * Editing an existing application also remembers the definition the edit started from, and a draft keeps it too.
 * Before the edit is saved, the application is read again; when someone else changed or deleted it in the meantime,
 * the operator sees what changed and chooses to keep editing or to save anyway.
 */
class PalantirServiceEditGuard {
    static STORAGE_PREFIX = "PalantirServiceDraft:";

    static DRAFT_LIFETIME_MILLISECONDS = 30 * 24 * 60 * 60 * 1000;

    static REDACTED = "\u2022\u2022\u2022 kept out of the draft \u2022\u2022\u2022";

    static REDACTION = 2;

    static SECRET_NAMES = /secret|password|passwd|passphrase|credential|privatekey|apikey|accesskey|signingkey|encryptionkey|^jwks$|^token$/i;

    static SECRET_METADATA = /^(expiration|expirationDate|expiresAt|expiry|createdAt|issuedAt|notBefore)$/i;

    static MODES = {
        wizard: {dialog: "#editServiceWizardDialog", editor: "wizardServiceEditor"},
        editor: {dialog: "#editServiceDialog", editor: "serviceEditor"}
    };

    static EDITABLE = "input, select, textarea, [role=switch], .ace_editor, .ts-wrapper, [contenteditable=true]";

    static state = {wizard: null, editor: null};

    static listening = new Set();

    static originals = new Map();

    static initialized = false;

    /**
     * The definition in a mode's JSON editor. The wizard's is first generated from any field edit still waiting on its
     * preview, so a field changed just before the dialog closes, is saved or handed over counts as an edit.
     *
     * @param mode wizard or editor
     * @returns {string} the definition text
     */
    static value(mode) {
        if (mode === "wizard" && typeof flushServiceDefinition === "function") {
            flushServiceDefinition();
        }
        const element = document.getElementById(PalantirServiceEditGuard.MODES[mode].editor);
        return element?.classList.contains("ace_editor") ? ace.edit(element).getValue() : "";
    }

    /**
     * A form of the definition that ignores formatting and the order of properties, so only real edits count. It is
     * the comparison the history uses, so reordering a map whose order matters still counts as an edit.
     *
     * @param text the definition text
     * @returns {string} the normalized definition, or the trimmed text when it is not JSON
     */
    static normalize(text) {
        try {
            return palantirStringifyJson(PalantirServiceHistory.sortKeys(palantirParseJson(text)));
        } catch (error) {
            return String(text ?? "").trim();
        }
    }

    /**
     * The storage key of a draft.
     *
     * @param mode wizard or editor
     * @param serviceId the numeric id of the application, or nothing for a new one
     * @param serviceClass the type of a new application
     * @returns {string} the key
     */
    static key(mode, serviceId, serviceClass = "") {
        return serviceId !== null && serviceId !== undefined && serviceId !== ""
            ? `${mode}:${serviceId}`
            : `${mode}:new:${serviceClass}`;
    }

    /**
     * Whether a kept draft is too old to offer, or has no time it was kept.
     *
     * @param draft the draft
     * @returns {boolean} true when the draft should be dropped
     */
    static isExpired(draft) {
        const savedAt = new Date(draft?.savedAt).getTime();
        return Number.isNaN(savedAt) || Date.now() - savedAt > PalantirServiceEditGuard.DRAFT_LIFETIME_MILLISECONDS;
    }

    static readDraft(key) {
        try {
            const stored = window.localStorage.getItem(PalantirServiceEditGuard.STORAGE_PREFIX + key);
            const draft = stored ? JSON.parse(stored) : null;
            if (draft && PalantirServiceEditGuard.isExpired(draft)) {
                PalantirServiceEditGuard.removeDraft(key);
                return null;
            }
            return draft ? PalantirServiceEditGuard.redactKept(PalantirServiceEditGuard.STORAGE_PREFIX + key, draft) : null;
        } catch (error) {
            return null;
        }
    }

    /**
     * Leave the secrets out of a kept draft again when it was kept by an earlier way of leaving them out, which may
     * have missed some, and keep it so. A draft left out the current way is returned as it is. When the browser cannot
     * keep it again, as when its storage is full, the earlier draft is removed instead, so it cannot keep a secret; the
     * draft without secrets is still returned, to be resumed this once.
     *
     * @param storageKey the storage key the draft is kept under
     * @param draft the draft as kept
     * @returns {object} the draft without secrets
     */
    static redactKept(storageKey, draft) {
        if (draft.secretsLeftOut === PalantirServiceEditGuard.REDACTION) {
            return draft;
        }
        const kept = PalantirServiceEditGuard.withoutSecrets(String(draft.text ?? ""));
        const redacted = {
            ...draft,
            text: kept.text,
            redacted: draft.redacted === true || kept.redacted,
            secretsLeftOut: PalantirServiceEditGuard.REDACTION,
            original: draft.original?.text
                ? {...draft.original, text: PalantirServiceEditGuard.withoutSecrets(String(draft.original.text)).text}
                : draft.original ?? null
        };
        try {
            window.localStorage.setItem(storageKey, JSON.stringify(redacted));
        } catch (error) {
            console.debug("Palantir could not keep the draft without its secrets, so it is removed", error);
            try {
                window.localStorage.removeItem(storageKey);
            } catch (removal) {
                console.debug("Palantir could not remove the draft kept with its secrets", removal);
            }
        }
        return redacted;
    }

    /**
     * Drop every kept draft that has expired, or that can no longer be read, and leave the secrets out again of a draft
     * kept by an earlier way of leaving them out, so no secret stays in the browser after an upgrade.
     */
    static removeExpiredDrafts() {
        try {
            const storage = window.localStorage;
            const keys = Array.from({length: storage.length}, (_, index) => storage.key(index))
                .filter(key => key?.startsWith(PalantirServiceEditGuard.STORAGE_PREFIX));
            for (const key of keys) {
                let draft = null;
                try {
                    draft = JSON.parse(storage.getItem(key));
                } catch (error) {
                    draft = null;
                }
                if (!draft || PalantirServiceEditGuard.isExpired(draft)) {
                    storage.removeItem(key);
                } else {
                    PalantirServiceEditGuard.redactKept(key, draft);
                }
            }
        } catch (error) {
            console.debug("Palantir could not look for expired drafts of applications", error);
        }
    }

    /**
     * Whether a setting holds a secret, judged by its name, such as a client secret, a password or a private key.
     *
     * @param name the name of the setting
     * @returns {boolean} true when its values are kept out of drafts
     */
    static isSecret(name) {
        return PalantirServiceEditGuard.SECRET_NAMES.test(String(name).replace(/[^a-z0-9]/gi, ""));
    }

    /**
     * Copy a definition with the values of its secrets replaced by a marker, so a draft kept in the browser holds no
     * secret. The type markers CAS writes, such as "@class" and the type before the items of a collection, are kept, and
     * so is what describes a secret without being one, such as when a client secret expires, so a draft keeps it as edited.
     *
     * @param value the definition or part of it
     * @param secret whether the value sits under a secret setting
     * @returns {*} the copy
     */
    static redact(value, secret = false) {
        if (Array.isArray(value)) {
            const typed = value.length === 2 && typeof value[0] === "string" && /^(java|org)\./.test(value[0]) && Array.isArray(value[1]);
            return typed
                ? [value[0], PalantirServiceEditGuard.redact(value[1], secret)]
                : value.map(entry => PalantirServiceEditGuard.redact(entry, secret));
        }
        if (value !== null && typeof value === "object") {
            return Object.fromEntries(Object.entries(value).map(([key, entry]) => [key, key === "@class"
                || (secret && PalantirServiceEditGuard.SECRET_METADATA.test(key))
                ? entry
                : PalantirServiceEditGuard.redact(entry, secret || PalantirServiceEditGuard.isSecret(key))]));
        }
        return secret && typeof value === "string" && value !== "" ? PalantirServiceEditGuard.REDACTED : value;
    }

    /**
     * Put back the secrets a draft left out, from the same place in the definition the dialog opens with, such as the
     * saved application. A secret it cannot tell keeps its marker, so the operator decides what it should be: the
     * definition cannot be saved until each marker is replaced or its setting removed, rather than an empty secret
     * silently replacing the saved one.
     *
     * @param value the draft definition or part of it
     * @param source the same part of the definition the dialog opens with
     * @param secret whether this part lies under a secret, such as the entries of the client secrets, which the draft
     *        left out whole
     * @returns {*} the definition with its secrets
     */
    static restore(value, source, secret = false) {
        if (value === PalantirServiceEditGuard.REDACTED) {
            return typeof source === "string" && source !== "" ? source : PalantirServiceEditGuard.REDACTED;
        }
        if (Array.isArray(value)) {
            const counterparts = PalantirServiceEditGuard.counterparts(value, Array.isArray(source) ? source : [], secret);
            return value.map((entry, index) => PalantirServiceEditGuard.restore(entry, counterparts[index], secret));
        }
        if (value !== null && typeof value === "object") {
            const from = source !== null && typeof source === "object" && !Array.isArray(source) ? source : {};
            return Object.fromEntries(Object.entries(value).map(([key, entry]) => [key, key === "@class"
                ? entry
                : PalantirServiceEditGuard.restore(entry, from[key], secret || PalantirServiceEditGuard.isSecret(key))]));
        }
        return value;
    }

    /**
     * Put back the secrets of the saved definition an edit started from, each from the very same place in the definition
     * the dialog opens with. That saved definition is only compared with the application as it is now, to tell whether
     * someone else changed it: where nobody did, the two are the same place for place, so its secrets come back exactly,
     * however alike its entries are; where somebody did, the comparison finds the change anyway.
     *
     * @param value the saved definition as the draft kept it, or part of it
     * @param source the same part of the definition the dialog opens with
     * @returns {*} the saved definition with its secrets
     */
    static restoreInPlace(value, source) {
        if (value === PalantirServiceEditGuard.REDACTED) {
            return typeof source === "string" && source !== PalantirServiceEditGuard.REDACTED ? source : "";
        }
        if (Array.isArray(value)) {
            const from = Array.isArray(source) ? source : [];
            return value.map((entry, index) => PalantirServiceEditGuard.restoreInPlace(entry, from[index]));
        }
        if (value !== null && typeof value === "object") {
            const from = source !== null && typeof source === "object" && !Array.isArray(source) ? source : {};
            return Object.fromEntries(Object.entries(value).map(([key, entry]) => [key, PalantirServiceEditGuard.restoreInPlace(entry, from[key])]));
        }
        return value;
    }

    /**
     * The entry of the definition the dialog opens with that each entry of a list in a draft came from. Entries such as
     * the policies of a chain may have been moved or removed in the draft, so an object is matched to an object of its
     * type it still equals once secrets are left out, whatever the order of their keys, except in maps that keep their
     * order, and only when that pairing is one to one: no other object of the draft equals it either, such as a copy the
     * operator made of it, which would otherwise take the secret by coming first. When several such objects equal it, it gets no secret at all: a draft cannot tell those objects apart, not
     * even by place, since swapping them leaves the draft as it was. The one object changed in the draft is matched to
     * the object at its place only when the list was neither reordered nor shortened: it kept its length and every
     * other object still equals the one at its own place. Otherwise, as when several objects changed, an entry that matches nothing gets
     * no secret, rather than possibly the secret of another.
     *
     * @param value the list in the draft
     * @param source the same list in the definition the dialog opens with
     * @param secret whether the list lies under a secret, so its entries are compared left out as the draft left them
     * @returns {Array} the counterpart of each entry, or undefined
     */
    static counterparts(value, source, secret = false) {
        const isObject = entry => entry !== null && typeof entry === "object" && !Array.isArray(entry);
        const canonical = entry => PalantirServiceHistory.canonical(entry);
        const identity = (entry, shape) => `${entry["@class"] ?? ""}\u0000${shape}`;
        const group = (entries, shapeOf) => {
            const groups = new Map();
            entries.forEach((entry, at) => {
                if (isObject(entry)) {
                    const id = identity(entry, shapeOf(entry));
                    const indexes = groups.get(id);
                    if (indexes) {
                        indexes.push(at);
                    } else {
                        groups.set(id, [at]);
                    }
                }
            });
            return groups;
        };
        const sources = group(source, candidate => canonical(PalantirServiceEditGuard.redact(candidate, secret)));
        const drafts = group(value, canonical);
        const used = new Set();
        const found = value.map((entry, index) => isObject(entry) ? undefined : source[index]);
        drafts.forEach((indexes, id) => {
            const candidates = sources.get(id) ?? [];
            if (indexes.length === 1 && candidates.length === 1) {
                used.add(candidates[0]);
                found[indexes[0]] = source[candidates[0]];
            }
        });
        const unmatched = value.filter((entry, index) => isObject(entry) && found[index] === undefined).length;
        const inPlace = unmatched === 1 && value.length === source.length
            && value.every((entry, index) => !isObject(entry) || found[index] === undefined || found[index] === source[index]);
        if (inPlace) {
            value.forEach((entry, index) => {
                if (isObject(entry) && found[index] === undefined && !used.has(index)
                    && isObject(source[index]) && source[index]["@class"] === entry["@class"]) {
                    used.add(index);
                    found[index] = source[index];
                }
            });
        }
        return found;
    }

    /**
     * A definition as text with its secrets left out. A definition that is not valid JSON yet, such as one in the
     * middle of being typed, is read string by string instead, keeping its layout.
     *
     * @param text the definition text
     * @returns {{text: string, redacted: boolean}} the text to keep, and whether a secret was left out
     */
    static withoutSecrets(text) {
        try {
            const kept = palantirStringifyJson(PalantirServiceEditGuard.redact(palantirParseJson(text)), 4);
            return kept.includes(JSON.stringify(PalantirServiceEditGuard.REDACTED))
                ? {text: kept, redacted: true}
                : {text: String(text), redacted: false};
        } catch (error) {
            const kept = PalantirServiceEditGuard.redactText(String(text ?? ""));
            return {text: kept, redacted: kept !== String(text ?? "")};
        }
    }

    /**
     * Leave the secrets out of definition text that may not be valid JSON, reading its strings, bare words and brackets
     * the way JSON or Hjson would: a value is a secret when it belongs to a secret setting, or sits anywhere inside one,
     * apart from type markers, which are an "@class" or the type that opens a list, and what describes a secret without being one, such as when it expires. A string that is
     * never closed is cut at the end of its line, and while a secret setting still waits for its value, whatever follows
     * it is taken for that value, even when it reads like the name of the next setting.
     *
     * @param text the definition text
     * @returns {string} the text with each secret replaced by a marker
     */
    static redactText(text) {
        const containers = [];
        const redacted = JSON.stringify(PalantirServiceEditGuard.REDACTED);
        let result = "";
        let key = null;
        let awaitingValue = false;
        let position = 0;
        const insideSecret = () => containers.at(-1)?.secret === true;
        const nextSignificant = from => {
            let index = from;
            while (index < text.length && /\s/.test(text[index])) {
                index++;
            }
            return text[index];
        };
        const secretValue = () => {
            const inObject = containers.at(-1)?.type === "{";
            if (insideSecret()) {
                return !(inObject && key !== null && PalantirServiceEditGuard.SECRET_METADATA.test(key));
            }
            return inObject && key !== null && PalantirServiceEditGuard.isSecret(key);
        };
        const pendingSecret = () => awaitingValue && secretValue();
        const readKey = (name, raw) => {
            key = name;
            awaitingValue = true;
            result += raw;
        };
        const readValue = (value, raw, literal) => {
            const container = containers.at(-1);
            const marker = container?.type === "{"
                ? key === "@class"
                : container?.type === "[" && container.entries === 0 && /^(java|org)\.[\w.$]+$/.test(value);
            result += secretValue() && !marker && !literal && value !== "" ? redacted : raw;
            awaitingValue = false;
            if (container) {
                container.entries++;
            }
        };
        while (position < text.length) {
            const character = text[position];
            if (character === "\"") {
                let end = position + 1;
                while (end < text.length && text[end] !== "\"" && text[end] !== "\n") {
                    end += text[end] === "\\" ? 2 : 1;
                }
                end = Math.min(end, text.length);
                const closed = text[end] === "\"";
                const value = text.slice(position + 1, end);
                const raw = text.slice(position, closed ? end + 1 : end);
                const isKey = containers.at(-1)?.type === "{" && closed && nextSignificant(end + 1) === ":" && !pendingSecret();
                if (isKey) {
                    readKey(value, raw);
                } else {
                    readValue(value, raw, false);
                }
                position = closed ? end + 1 : end;
                continue;
            }
            if (/[^\s{}\[\],:"]/.test(character)) {
                let end = position;
                while (end < text.length && /[^\s{}\[\],:"]/.test(text[end])) {
                    end++;
                }
                const word = text.slice(position, end);
                const isKey = containers.at(-1)?.type === "{" && nextSignificant(end) === ":" && !pendingSecret();
                if (isKey) {
                    readKey(word, word);
                } else {
                    readValue(word, word, /^(true|false|null)$/.test(word));
                }
                position = end;
                continue;
            }
            if (character === "{" || character === "[") {
                const parent = containers.at(-1);
                containers.push({type: character, secret: insideSecret() || secretValue(), entries: 0});
                if (parent) {
                    parent.entries++;
                }
                key = null;
                awaitingValue = false;
            } else if (character === "}" || character === "]") {
                containers.pop();
                key = null;
                awaitingValue = false;
            } else if (character === ",") {
                key = containers.at(-1)?.type === "{" ? null : key;
                awaitingValue = false;
            }
            result += character;
            position++;
        }
        return result;
    }

    static writeDraft(key, text) {
        const original = PalantirServiceEditGuard.originals.get(key) ?? null;
        const kept = PalantirServiceEditGuard.withoutSecrets(text);
        try {
            window.localStorage.setItem(PalantirServiceEditGuard.STORAGE_PREFIX + key, JSON.stringify({
                text: kept.text,
                redacted: kept.redacted,
                secretsLeftOut: PalantirServiceEditGuard.REDACTION,
                savedAt: new Date().toISOString(),
                original: original === null ? null : {...original, text: PalantirServiceEditGuard.withoutSecrets(original.text).text}
            }));
        } catch (error) {
            console.debug("Palantir could not keep a draft of the application", error);
        }
    }

    static removeDraft(key) {
        try {
            window.localStorage.removeItem(PalantirServiceEditGuard.STORAGE_PREFIX + key);
        } catch (error) {
            console.debug("Palantir could not remove the draft of the application", error);
        }
    }

    /**
     * Offer to resume a draft kept for the application, if there is one. Starting over removes the draft.
     *
     * @param key the storage key of the draft
     * @param differsFrom the definition the dialog opens with; a draft equal to it is dropped without asking
     * @returns {Promise<string|null>} the draft text to resume, or null
     */
    static async offerDraft(key, differsFrom = null) {
        const stored = PalantirServiceEditGuard.readDraft(key);
        if (!stored?.text) {
            return null;
        }
        const draft = PalantirServiceEditGuard.withSecretsFrom(stored, differsFrom);
        if (differsFrom !== null && PalantirServiceEditGuard.normalize(draft.text) === PalantirServiceEditGuard.normalize(differsFrom)) {
            PalantirServiceEditGuard.removeDraft(key);
            return null;
        }
        const savedAt = new Date(draft.savedAt);
        const unmatched = PalantirServiceEditGuard.leftOut(draft.text);
        const result = await Swal.fire({
            title: "Resume Your Draft?",
            text: `Unsaved changes to this application were kept${Number.isNaN(savedAt.getTime()) ? "" : ` on ${savedAt.toLocaleString()}`}.`
                + (stored.redacted ? " Drafts keep no secrets, such as client secrets or passwords: the saved application's are used, "
                    + "and any you had changed need to be entered again." : "")
                + (unmatched > 0 ? ` ${unmatched} secret${unmatched === 1 ? "" : "s"} could not be told apart from the saved application, `
                    + "and still show as left out: enter each again, or remove its setting, before saving." : ""),
            icon: "question",
            showDenyButton: true,
            confirmButtonText: "Resume Draft",
            denyButtonText: "Start Over",
            allowOutsideClick: false,
            allowEscapeKey: false
        });
        if (result.isConfirmed) {
            if (draft.original?.text) {
                PalantirServiceEditGuard.originals.set(key, draft.original);
            }
            return draft.text;
        }
        PalantirServiceEditGuard.removeDraft(key);
        return null;
    }

    /**
     * A kept draft with the secrets it left out put back from the definition the dialog opens with.
     *
     * @param draft the draft as kept
     * @param source the definition text the dialog opens with, or nothing
     * @returns {object} the draft with its secrets
     */
    static withSecretsFrom(draft, source) {
        const fill = (text, restore) => {
            try {
                let from = null;
                try {
                    from = source === null || source === undefined ? null : palantirParseJson(source);
                } catch (error) {
                    from = null;
                }
                return palantirStringifyJson(restore(palantirParseJson(text), from), 4);
            } catch (error) {
                return text;
            }
        };
        const marked = text => String(text ?? "").includes(PalantirServiceEditGuard.REDACTED);
        if (!marked(draft.text) && !marked(draft.original?.text)) {
            return draft;
        }
        return {
            ...draft,
            text: fill(draft.text, PalantirServiceEditGuard.restore),
            original: draft.original?.text
                ? {...draft.original, text: fill(draft.original.text, PalantirServiceEditGuard.restoreInPlace)}
                : draft.original
        };
    }

    /**
     * Remember the saved definition an edit of an existing application starts from.
     *
     * @param key the storage key of the application's draft
     * @param definition the definition as the server returned it
     */
    static remember(key, definition) {
        if (definition?.id !== null && definition?.id !== undefined) {
            PalantirServiceEditGuard.originals.set(key, {id: String(definition.id), text: palantirStringifyJson(definition)});
        }
    }

    /**
     * Hold a dialog still from the moment its definition is taken to be saved until CAS answers, so nothing typed in
     * the meantime can be lost: a save that succeeds closes the dialog and removes its draft, which would otherwise
     * take along edits made after the definition was sent.
     *
     * @param mode wizard or editor
     * @returns {function(): void} lets the dialog be edited again; calling it more than once does nothing more
     */
    static holdWhileSaving(mode) {
        const content = document.querySelector(PalantirServiceEditGuard.MODES[mode].dialog);
        const dialog = content?.closest(".ui-dialog") ?? content;
        if (!dialog) {
            return () => {};
        }
        dialog.inert = true;
        dialog.setAttribute("aria-busy", "true");
        dialog.classList.add("palantir-dialog--saving");
        let held = true;
        return () => {
            if (held) {
                held = false;
                dialog.inert = false;
                dialog.removeAttribute("aria-busy");
                dialog.classList.remove("palantir-dialog--saving");
            }
        };
    }

    /**
     * How many secrets a definition still leaves out, shown by their markers.
     *
     * @param text the definition text
     * @returns {number} the number of markers
     */
    static leftOut(text) {
        return String(text ?? "").split(PalantirServiceEditGuard.REDACTED).length - 1;
    }

    /**
     * Refuse to save a definition that still holds a secret a draft left out, as one resumed from a draft that was not
     * valid JSON, or whose secret could not be told apart, keeps it as a marker: saving it would make the marker the
     * secret, and leaving it empty would discard the saved one.
     *
     * @param text the definition about to be saved
     * @returns {boolean} true when the definition may be saved
     */
    static refuseLeftOutSecrets(text) {
        if (PalantirServiceEditGuard.leftOut(text) === 0) {
            return true;
        }
        Swal.fire({
            title: "Enter the Left-Out Secrets",
            text: `Drafts keep no secrets, and this definition still shows "${PalantirServiceEditGuard.REDACTED}" where one was. `
                + "Enter each such secret again, or remove the setting, before saving.",
            icon: "warning"
        });
        return false;
    }

    /**
     * Read the application again before an edit is saved, and ask the operator what to do when someone else changed
     * or deleted it since the edit started. An application whose id was changed in the edit, or that cannot be
     * read for another reason, is saved as usual.
     *
     * @param mode wizard or editor
     * @param text the definition about to be saved
     * @returns {Promise<string>} save when nothing changed, overwrite when the operator chose to save anyway, or
     * cancel to keep editing
     */
    static async checkConflict(mode, text) {
        const state = PalantirServiceEditGuard.state[mode];
        const original = state ? PalantirServiceEditGuard.originals.get(state.key) : null;
        let id;
        try {
            id = String(palantirParseJson(text).id);
        } catch (error) {
            id = null;
        }
        if (!original || id !== original.id) {
            return "save";
        }
        let current = null;
        try {
            current = await $.ajax({
                url: `${CasActuatorEndpoints.registeredServices()}/${encodeURIComponent(id)}`,
                dataType: "json",
                headers: {"Accept": "application/json"}
            });
        } catch (xhr) {
            if (xhr?.status !== 404) {
                return "save";
            }
        }
        if (current && PalantirServiceEditGuard.normalize(palantirStringifyJson(current)) === PalantirServiceEditGuard.normalize(original.text)) {
            return "save";
        }
        const message = current
            ? "Someone saved changes to this application after you started editing it. Saving yours replaces their changes."
            : "Someone deleted this application after you started editing it. Saving yours creates it again.";
        const diff = current
            ? `<div class="palantir-conflict__diff">${PalantirServiceHistory.table("When you started editing", "Saved since",
                PalantirServiceHistory.compare(PalantirServiceHistory.canonical(palantirParseJson(original.text)),
                    PalantirServiceHistory.canonical(current)))}</div>`
            : "";
        const result = await Swal.fire({
            title: current ? "This Application Has Changed" : "This Application Was Deleted",
            html: `<p class="palantir-conflict__message">${escapePalantirMarkup(message)}</p>${diff}`,
            icon: "warning",
            width: Math.min(1100, $(window).width() - 32),
            showCancelButton: true,
            confirmButtonText: "Save Mine Anyway",
            cancelButtonText: "Keep Editing",
            focusCancel: true,
            customClass: {popup: "palantir-conflict"},
            didOpen: popup => {
                $(popup).on("click", "[data-diff-fold]", event => PalantirServiceHistory.unfold(event.currentTarget));
            }
        });
        return result.isConfirmed ? "overwrite" : "cancel";
    }

    /**
     * Whether a key press changes text: a character typed, a deletion, or pasting, cutting or undoing; copying, moving
     * around and other shortcuts do not.
     *
     * @param event the key press
     * @returns {boolean} true when the key press edits
     */
    static edits(event) {
        const key = String(event.key ?? "");
        if ((event.ctrlKey || event.metaKey) && !event.altKey) {
            return ["v", "x", "z", "y"].includes(key.toLowerCase());
        }
        return key.length === 1 || key === "Backspace" || key === "Delete";
    }

    /**
     * Start guarding a dialog that has just opened. The baseline is taken once the dialog settles: right away for
     * the editor, and for the wizard once it has finished filling its fields and its generated definition has stayed
     * the same for a moment, since the definition is generated again after the fields are filled. Someone may start
     * editing before then, so the first key typed or click on a field in the dialog takes the baseline at once, from
     * what the dialog holds before that edit reaches it, and the edit counts as unsaved. A resumed draft is unsaved
     * by definition, so closing it asks until it is saved or discarded.
     *
     * @param mode wizard or editor
     * @param key the storage key of the application's draft
     * @param resumed whether the dialog opened on a resumed draft
     */
    static watch(mode, key, resumed = false) {
        const state = {key: key, baseline: null, resumed: resumed, allowClose: false, keepDraft: false, timer: null};
        PalantirServiceEditGuard.state[mode] = state;
        PalantirServiceEditGuard.listen(mode);
        const watching = new AbortController();
        const claim = event => {
            if (PalantirServiceEditGuard.state[mode] !== state || state.baseline !== null) {
                watching.abort();
                return;
            }
            const populating = mode === "wizard" && typeof isPopulatingWizard !== "undefined" && isPopulatingWizard;
            const editing = event.type === "keydown"
                ? PalantirServiceEditGuard.edits(event)
                : event.target instanceof Element && event.target.closest(PalantirServiceEditGuard.EDITABLE) !== null;
            if (event.isTrusted && editing && !populating) {
                state.baseline = PalantirServiceEditGuard.normalize(PalantirServiceEditGuard.value(mode));
                watching.abort();
            }
        };
        const dialog = document.querySelector(PalantirServiceEditGuard.MODES[mode].dialog);
        ["pointerdown", "keydown"].forEach(type => dialog?.addEventListener(type, claim, {capture: true, signal: watching.signal}));
        const settle = (attempts = 0, previous = null, checks = 0) => {
            if (PalantirServiceEditGuard.state[mode] !== state || state.baseline !== null) {
                watching.abort();
                return;
            }
            if (mode === "wizard" && typeof isPopulatingWizard !== "undefined" && isPopulatingWizard && attempts < 100) {
                setTimeout(() => settle(attempts + 1), 100);
                return;
            }
            const current = PalantirServiceEditGuard.normalize(PalantirServiceEditGuard.value(mode));
            if (mode === "wizard" && current !== previous && checks < 3) {
                setTimeout(() => settle(attempts, current, checks + 1), 300);
                return;
            }
            state.baseline = current;
            watching.abort();
        };
        setTimeout(() => settle(), mode === "wizard" ? 500 : 0);
    }

    /**
     * Follow a mode's JSON editor, keeping the draft in step with the edits.
     *
     * @param mode wizard or editor
     */
    static listen(mode) {
        if (PalantirServiceEditGuard.listening.has(mode)) {
            return;
        }
        const element = document.getElementById(PalantirServiceEditGuard.MODES[mode].editor);
        if (!element?.classList.contains("ace_editor")) {
            return;
        }
        PalantirServiceEditGuard.listening.add(mode);
        ace.edit(element).session.on("change", () => {
            const state = PalantirServiceEditGuard.state[mode];
            if (!state) {
                return;
            }
            clearTimeout(state.timer);
            state.timer = setTimeout(() => PalantirServiceEditGuard.track(mode), 400);
        });
    }

    /**
     * Whether a mode's dialog holds edits that are not saved. A resumed draft, or edits handed over from the other
     * dialog, are unsaved from the moment the dialog opens, also while the wizard is still filling its fields.
     *
     * @param mode wizard or editor
     * @returns {boolean} true when closing would lose edits
     */
    static isDirty(mode) {
        const state = PalantirServiceEditGuard.state[mode];
        if (!state) {
            return false;
        }
        if (state.resumed) {
            return true;
        }
        return state.baseline !== null && PalantirServiceEditGuard.normalize(PalantirServiceEditGuard.value(mode)) !== state.baseline;
    }

    /**
     * Keep the draft while the edits are unsaved and remove it once they are undone.
     *
     * @param mode wizard or editor
     */
    static track(mode) {
        const state = PalantirServiceEditGuard.state[mode];
        if (!state || state.baseline === null) {
            return;
        }
        if (PalantirServiceEditGuard.isDirty(mode)) {
            PalantirServiceEditGuard.writeDraft(state.key, PalantirServiceEditGuard.value(mode));
        } else {
            PalantirServiceEditGuard.removeDraft(state.key);
        }
    }

    /**
     * Let a dialog close because its edits move to the other dialog, such as from the JSON editor to the wizard. Its
     * draft is kept for now, and the caller removes it once the other dialog holds the edits.
     *
     * @param mode wizard or editor
     * @returns {object|null} the dialog's state, with the key of its draft, or null when it is not guarded
     */
    static handOver(mode) {
        const state = PalantirServiceEditGuard.state[mode];
        if (state) {
            clearTimeout(state.timer);
            state.allowClose = true;
            state.keepDraft = true;
        }
        return state ?? null;
    }

    /**
     * The definition was saved: drop the draft and let the dialog close.
     *
     * @param mode wizard or editor
     */
    static saved(mode) {
        const state = PalantirServiceEditGuard.state[mode];
        if (state) {
            clearTimeout(state.timer);
            PalantirServiceEditGuard.removeDraft(state.key);
            state.allowClose = true;
        }
    }

    /**
     * Ask before a dialog with unsaved edits closes. Returning false keeps the dialog open while the question is
     * asked; the answer closes it again when the edits are kept as a draft or discarded. Keep Editing leaves it open
     * and triggers <code>palantir:closecancelled</code> on the dialog, for a caller that waits for the close.
     *
     * @param mode wizard or editor
     * @returns {boolean} whether the dialog may close now
     */
    static beforeClose(mode) {
        const state = PalantirServiceEditGuard.state[mode];
        if (!state || state.allowClose || !PalantirServiceEditGuard.isDirty(mode)) {
            return true;
        }
        Swal.fire({
            title: "Discard Your Changes?",
            text: "This application has changes that are not saved.",
            icon: "warning",
            showDenyButton: true,
            showCancelButton: true,
            confirmButtonText: "Save as Draft",
            denyButtonText: "Discard Changes",
            cancelButtonText: "Keep Editing",
            focusCancel: true
        }).then(result => {
            if (result.isConfirmed) {
                if (state.baseline !== null || PalantirServiceEditGuard.readDraft(state.key) === null) {
                    PalantirServiceEditGuard.writeDraft(state.key, PalantirServiceEditGuard.value(mode));
                }
                state.keepDraft = true;
            } else if (result.isDenied) {
                PalantirServiceEditGuard.removeDraft(state.key);
            } else {
                $(PalantirServiceEditGuard.MODES[mode].dialog).trigger("palantir:closecancelled");
                return;
            }
            clearTimeout(state.timer);
            state.allowClose = true;
            $(PalantirServiceEditGuard.MODES[mode].dialog).dialog("close");
        });
        return false;
    }

    /**
     * Stop guarding a dialog that has closed. Unless the operator chose to keep a draft, a draft written while the
     * edits were unsaved is stale now: the dialog closed without them, after they were saved, discarded or undone.
     *
     * @param mode wizard or editor
     */
    static closed(mode) {
        const state = PalantirServiceEditGuard.state[mode];
        if (state) {
            clearTimeout(state.timer);
            if (!state.keepDraft) {
                PalantirServiceEditGuard.removeDraft(state.key);
            }
            PalantirServiceEditGuard.originals.delete(state.key);
        }
        PalantirServiceEditGuard.state[mode] = null;
    }

    /**
     * Drop expired drafts, and warn before the page is left while a dialog holds unsaved edits; the draft is kept
     * either way.
     */
    static initialize() {
        if (PalantirServiceEditGuard.initialized) {
            return;
        }
        PalantirServiceEditGuard.initialized = true;
        PalantirServiceEditGuard.removeExpiredDrafts();
        window.addEventListener("beforeunload", event => {
            const dirty = Object.keys(PalantirServiceEditGuard.MODES).filter(mode => PalantirServiceEditGuard.isDirty(mode));
            if (dirty.length > 0) {
                dirty.forEach(mode => PalantirServiceEditGuard.track(mode));
                event.preventDefault();
                event.returnValue = "";
            }
        });
    }
}

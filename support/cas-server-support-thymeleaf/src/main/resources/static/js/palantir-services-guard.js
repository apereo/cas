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

    static SECRET_NAMES = /secret|password|passwd|passphrase|credential|privatekey|apikey|accesskey|signingkey|encryptionkey|^jwks$|^token$/i;

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
     * The definition in a mode's JSON editor.
     *
     * @param mode wizard or editor
     * @returns {string} the definition text
     */
    static value(mode) {
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
            return JSON.stringify(PalantirServiceHistory.sortKeys(JSON.parse(text)));
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
            return draft;
        } catch (error) {
            return null;
        }
    }

    /**
     * Drop every kept draft that has expired, or that can no longer be read, and leave the secrets out of a draft kept
     * before drafts left them out, so no secret stays in the browser after an upgrade.
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
                } else if (draft.secretsLeftOut !== true) {
                    const kept = PalantirServiceEditGuard.withoutSecrets(String(draft.text ?? ""));
                    storage.setItem(key, JSON.stringify({
                        ...draft,
                        text: kept.text,
                        redacted: draft.redacted === true || kept.redacted,
                        secretsLeftOut: true,
                        original: draft.original?.text
                            ? {...draft.original, text: PalantirServiceEditGuard.withoutSecrets(String(draft.original.text)).text}
                            : draft.original ?? null
                    }));
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
     * secret. The type markers CAS writes, such as "@class" and the type before the items of a collection, are kept.
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
                ? entry
                : PalantirServiceEditGuard.redact(entry, secret || PalantirServiceEditGuard.isSecret(key))]));
        }
        return secret && typeof value === "string" && value !== "" ? PalantirServiceEditGuard.REDACTED : value;
    }

    /**
     * Put back the secrets a draft left out, from the same place in the definition the dialog opens with, such as the
     * saved application; a secret it does not have is left empty.
     *
     * @param value the draft definition or part of it
     * @param source the same part of the definition the dialog opens with
     * @returns {*} the definition with its secrets
     */
    static restore(value, source) {
        if (value === PalantirServiceEditGuard.REDACTED) {
            return typeof source === "string" && source !== PalantirServiceEditGuard.REDACTED ? source : "";
        }
        if (Array.isArray(value)) {
            return value.map((entry, index) => PalantirServiceEditGuard.restore(entry, Array.isArray(source) ? source[index] : undefined));
        }
        if (value !== null && typeof value === "object") {
            const from = source !== null && typeof source === "object" && !Array.isArray(source) ? source : {};
            return Object.fromEntries(Object.entries(value).map(([key, entry]) => [key, PalantirServiceEditGuard.restore(entry, from[key])]));
        }
        return value;
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
            const kept = JSON.stringify(PalantirServiceEditGuard.redact(JSON.parse(text)), null, 4);
            return kept.includes(JSON.stringify(PalantirServiceEditGuard.REDACTED))
                ? {text: kept, redacted: true}
                : {text: String(text), redacted: false};
        } catch (error) {
            const kept = PalantirServiceEditGuard.redactText(String(text ?? ""));
            return {text: kept, redacted: kept !== String(text ?? "")};
        }
    }

    /**
     * Leave the secrets out of definition text that may not be valid JSON, reading its strings and brackets the way
     * JSON would: a string is a secret when it is the value of a secret setting, or sits anywhere inside one, apart
     * from type markers. A string that is never closed is cut at the end of its line.
     *
     * @param text the definition text
     * @returns {string} the text with each secret replaced by a marker
     */
    static redactText(text) {
        const containers = [];
        let result = "";
        let key = null;
        let position = 0;
        const insideSecret = () => containers.at(-1)?.secret === true;
        const nextSignificant = from => {
            let index = from;
            while (index < text.length && /\s/.test(text[index])) {
                index++;
            }
            return text[index];
        };
        while (position < text.length) {
            const character = text[position];
            if (character === "\"") {
                let end = position + 1;
                while (end < text.length && text[end] !== "\"" && text[end] !== "\n") {
                    end += text[end] === "\\" ? 2 : 1;
                }
                const closed = text[end] === "\"";
                const value = text.slice(position + 1, Math.min(end, text.length));
                const container = containers.at(-1);
                const isKey = container?.type === "{" && closed && nextSignificant(end + 1) === ":";
                if (isKey) {
                    key = value;
                    result += text.slice(position, end + 1);
                } else {
                    const secret = insideSecret() || (container?.type === "{" && key !== null && PalantirServiceEditGuard.isSecret(key));
                    const marker = key === "@class" || /^(java|org)\./.test(value);
                    result += secret && !marker && value !== ""
                        ? JSON.stringify(PalantirServiceEditGuard.REDACTED)
                        : text.slice(position, closed ? end + 1 : end);
                }
                position = closed ? end + 1 : end;
                continue;
            }
            if (character === "{" || character === "[") {
                const container = containers.at(-1);
                containers.push({
                    type: character,
                    secret: insideSecret() || (container?.type === "{" && key !== null && PalantirServiceEditGuard.isSecret(key))
                });
                key = null;
            } else if (character === "}" || character === "]") {
                containers.pop();
                key = null;
            } else if (character === ",") {
                key = containers.at(-1)?.type === "{" ? null : key;
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
                secretsLeftOut: true,
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
        const result = await Swal.fire({
            title: "Resume Your Draft?",
            text: `Unsaved changes to this application were kept${Number.isNaN(savedAt.getTime()) ? "" : ` on ${savedAt.toLocaleString()}`}.`
                + (stored.redacted ? " Drafts keep no secrets, such as client secrets or passwords: the saved application's are used, "
                    + "and any you had changed need to be entered again." : ""),
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
        const fill = text => {
            try {
                let from = null;
                try {
                    from = source === null || source === undefined ? null : JSON.parse(source);
                } catch (error) {
                    from = null;
                }
                return JSON.stringify(PalantirServiceEditGuard.restore(JSON.parse(text), from), null, 4);
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
            text: fill(draft.text),
            original: draft.original?.text ? {...draft.original, text: fill(draft.original.text)} : draft.original
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
            PalantirServiceEditGuard.originals.set(key, {id: String(definition.id), text: JSON.stringify(definition)});
        }
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
            id = String(JSON.parse(text).id);
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
        if (current && PalantirServiceEditGuard.normalize(JSON.stringify(current)) === PalantirServiceEditGuard.normalize(original.text)) {
            return "save";
        }
        const message = current
            ? "Someone saved changes to this application after you started editing it. Saving yours replaces their changes."
            : "Someone deleted this application after you started editing it. Saving yours creates it again.";
        const diff = current
            ? `<div class="palantir-conflict__diff">${PalantirServiceHistory.table("When you started editing", "Saved since",
                PalantirServiceHistory.compare(PalantirServiceHistory.canonical(JSON.parse(original.text)),
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

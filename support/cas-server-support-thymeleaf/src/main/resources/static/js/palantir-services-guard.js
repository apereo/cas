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

    static MODES = {
        wizard: {dialog: "#editServiceWizardDialog", editor: "wizardServiceEditor"},
        editor: {dialog: "#editServiceDialog", editor: "serviceEditor"}
    };

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
     * A form of the definition that ignores formatting and the order of properties, so only real edits count.
     *
     * @param text the definition text
     * @returns {string} the normalized definition, or the trimmed text when it is not JSON
     */
    static normalize(text) {
        const sort = value => {
            if (Array.isArray(value)) {
                return value.map(sort);
            }
            if (value !== null && typeof value === "object") {
                return Object.keys(value).sort().reduce((sorted, key) => ({...sorted, [key]: sort(value[key])}), {});
            }
            return value;
        };
        try {
            return JSON.stringify(sort(JSON.parse(text)));
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
     * Drop every kept draft that has expired, or that can no longer be read.
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
                }
            }
        } catch (error) {
            console.debug("Palantir could not look for expired drafts of applications", error);
        }
    }

    static writeDraft(key, text) {
        try {
            window.localStorage.setItem(PalantirServiceEditGuard.STORAGE_PREFIX + key, JSON.stringify({
                text: text,
                savedAt: new Date().toISOString(),
                original: PalantirServiceEditGuard.originals.get(key) ?? null
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
        const draft = PalantirServiceEditGuard.readDraft(key);
        if (!draft?.text) {
            return null;
        }
        if (differsFrom !== null && PalantirServiceEditGuard.normalize(draft.text) === PalantirServiceEditGuard.normalize(differsFrom)) {
            PalantirServiceEditGuard.removeDraft(key);
            return null;
        }
        const savedAt = new Date(draft.savedAt);
        const result = await Swal.fire({
            title: "Resume your draft?",
            text: `Unsaved changes to this application were kept${Number.isNaN(savedAt.getTime()) ? "" : ` on ${savedAt.toLocaleString()}`}.`,
            icon: "question",
            showDenyButton: true,
            confirmButtonText: "Resume draft",
            denyButtonText: "Start over",
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
            title: current ? "This application has changed" : "This application was deleted",
            html: `<p class="palantir-conflict__message">${escapePalantirMarkup(message)}</p>${diff}`,
            icon: "warning",
            width: Math.min(1100, $(window).width() - 32),
            showCancelButton: true,
            confirmButtonText: "Save mine anyway",
            cancelButtonText: "Keep editing",
            focusCancel: true,
            customClass: {popup: "palantir-conflict"},
            didOpen: popup => {
                $(popup).on("click", "[data-diff-fold]", event => PalantirServiceHistory.unfold(event.currentTarget));
            }
        });
        return result.isConfirmed ? "overwrite" : "cancel";
    }

    /**
     * Start guarding a dialog that has just opened. The baseline is taken once the dialog settles: right away for
     * the editor, and for the wizard once it has finished filling its fields and its generated definition has stayed
     * the same for a moment, since the definition is generated again after the fields are filled. A resumed
     * draft is unsaved by definition, so closing it asks until it is saved or discarded.
     *
     * @param mode wizard or editor
     * @param key the storage key of the application's draft
     * @param resumed whether the dialog opened on a resumed draft
     */
    static watch(mode, key, resumed = false) {
        const state = {key: key, baseline: null, resumed: resumed, allowClose: false, keepDraft: false, timer: null};
        PalantirServiceEditGuard.state[mode] = state;
        PalantirServiceEditGuard.listen(mode);
        const settle = (attempts = 0, previous = null, checks = 0) => {
            if (PalantirServiceEditGuard.state[mode] !== state) {
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
     * Whether a mode's dialog holds edits that are not saved.
     *
     * @param mode wizard or editor
     * @returns {boolean} true when closing would lose edits
     */
    static isDirty(mode) {
        const state = PalantirServiceEditGuard.state[mode];
        if (!state || state.baseline === null) {
            return false;
        }
        return state.resumed || PalantirServiceEditGuard.normalize(PalantirServiceEditGuard.value(mode)) !== state.baseline;
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
     * asked; the answer closes it again when the edits are kept as a draft or discarded.
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
            title: "Discard your changes?",
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
                PalantirServiceEditGuard.writeDraft(state.key, PalantirServiceEditGuard.value(mode));
                state.keepDraft = true;
            } else if (result.isDenied) {
                PalantirServiceEditGuard.removeDraft(state.key);
            } else {
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

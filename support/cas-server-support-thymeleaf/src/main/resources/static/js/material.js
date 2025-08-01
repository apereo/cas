let cas = {
    init: () => {
        cas.attachFields();
        mdc.autoInit();
    },
    openMenu: () => {
        const menu = new mdc.menu.MDCMenu(document.querySelector('.mdc-menu'));
        menu.open = true;
    },
    attachFields: () => {
        let divs = document.querySelectorAll('.mdc-text-field');
        for (const div of divs) {
            new mdc.textField.MDCTextField(div);
            let field = mdc.textField.MDCTextField.attachTo(div);
            if (div.classList.contains('caps-check')) {
                field.foundation.adapter.registerInputInteractionHandler('keypress', cas.checkCaps);
            }
        }

        let helpers = document.querySelectorAll('.mdc-text-field-helper-text');
        for (const helper of helpers) {
            new mdc.textField.MDCTextFieldHelperText(helper);
        }
        
        let selectors = document.querySelectorAll('.mdc-select');
        for (const selector of selectors) {
            const select = new mdc.select.MDCSelect(selector);
            select.listen('MDCSelect:change', () => $("#source").val(select.value));
            $('#source').val(select.value);
        }

        let tooltips = document.querySelectorAll('.mdc-tooltip');
        if (tooltips != null) {
            tooltips.forEach(t => new mdc.tooltip.MDCTooltip(t))
        }
        let banners = document.querySelectorAll('.mdc-banner');
        if (banners != null) {
            banners.forEach(b => new mdc.banner.MDCBanner(b))
        }
        let dialogs = document.querySelectorAll('.mdc-dialog');
        if (dialogs != null) {
            dialogs.forEach(b => new mdc.dialog.MDCDialog.attachTo(b))
        }

        for (const el of document.querySelectorAll('.mdc-switch')) {
            let switchElement = new mdc.switchControl.MDCSwitch(el);
            const switchInputs = document.querySelectorAll(`input[data-switch-btn="${el.id}"]`);
            if (switchInputs.length === 1) {
                el.addEventListener('click', () => {
                    const switchInput = switchInputs[0];
                    console.log(`Clicked switch element "${switchInput.id}": ${switchElement.selected}`);
                    switchInput.value = switchElement.selected;
                });
            }
        }

        for (const el of document.querySelectorAll('.mdc-menu')) {
            new mdc.menu.MDCMenu(el);
        }

        for (const el of document.querySelectorAll('.mdc-linear-progress')) {
            new mdc.linearProgress.MDCLinearProgress(el);
        }

        for (const el of document.querySelectorAll('.mdc-data-table')) {
            new mdc.dataTable.MDCDataTable(el);
        }
        for (const el of document.querySelectorAll('.mdc-snackbar')) {
            new mdc.snackbar.MDCSnackbar(el);
        }

        let elms = document.querySelectorAll('.mdc-tab-bar');
        for (const elm of elms) {
            let tabs = mdc.tabBar.MDCTabBar.attachTo(elm);

            tabs.listen('MDCTabBar:activated', ev => {
                let index = ev.detail.index;
                $('.attribute-tab').addClass('d-none');
                $(`#attribute-tab-${index}`).removeClass('d-none');
            });
            tabs.foundation.adapter.activateTabAtIndex(0);
        }

        for (const el of document.querySelectorAll('.mdc-fab')) {
           new mdc.ripple.MDCRipple(el);
        }

    },
    checkCaps: (ev) => {
        if (ev.getModifierState("CapsLock")) {
            $(".caps-warn").removeClass('caps-warn').addClass('caps-on');
        } else {
            $(".caps-on").removeClass('caps-on').addClass('caps-warn');
        }
    },
    openDialog: (id) => {
        const dialog = new mdc.dialog.MDCDialog(document.getElementById(id));
        dialog.open();
        return false;
    },
};

let header = {
    init: () => {
        header.attachTopbar();
        mdc.autoInit();
    },
    attachDrawer: () => {
        let elm = document.getElementById('app-drawer');
        if (elm != null) {
            let drawer = mdc.drawer.MDCDrawer.attachTo(elm);
            let closeDrawer = evt => {
                drawer.open = false;
            };
            drawer.foundation.handleScrimClick = closeDrawer;
            document.onkeydown = evt => {
                evt = evt || window.event;
                if (evt.keyCode === 27) {
                    closeDrawer();
                }
            };
            header.drawer = drawer;
            return drawer;
        }
        return undefined;
    },
    attachTopbar: drawer => {

        drawer = header.attachDrawer();
        let dialog = header.attachNotificationDialog();

        if (drawer !== undefined) {
            header.attachDrawerToggle(drawer);
        }
        if (dialog !== undefined) {
            header.attachNotificationToggle(dialog);
        }
    },
    attachDrawerToggle: drawer => {
        let appBar = document.getElementById('app-bar');
        if (appBar != null) {
            let topAppBar = mdc.topAppBar.MDCTopAppBar.attachTo(appBar);
            topAppBar.setScrollTarget(document.getElementById('main-content'));
            topAppBar.listen('MDCTopAppBar:nav', () => {
                drawer.open = !drawer.open;
            });
            return topAppBar;
        }
        return undefined;
    },
    attachNotificationDialog: () => {
        let element = document.getElementById('cas-notification-dialog');
        if (element != null) {
            return mdc.dialog.MDCDialog.attachTo(element);
        }
        return undefined;
    },
    attachNotificationToggle: dialog => {
        let btn = document.getElementById('cas-notifications-menu');
        if (btn != null) {
            btn.addEventListener('click', () => dialog.open());
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    cas.init();
    header.init();
});

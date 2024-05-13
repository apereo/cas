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
        new mdc.textField.MDCTextFieldHelperText(document.querySelectorAll('.mdc-text-field-helper-text'));

        let divs = document.querySelectorAll('.mdc-text-field');

        for (const div of divs) {
            const textField = new mdc.textField.MDCTextField(f);
            textField.focus();
            let field = mdc.textField.MDCTextField.attachTo(div);
            if (div.classList.contains('caps-check')) {
                field.foundation.adapter.registerInputInteractionHandler('keypress', cas.checkCaps);
            }
        }

        let selector = document.querySelector('.mdc-select');
        if (selector != null) {
            const select = new mdc.select.MDCSelect(selector);
            select.listen('MDCSelect:change', () => {
                $('#source').val(select.value);
            });
            $('#source').val(select.value);
        }

        let tooltips = document.querySelectorAll('.mdc-tooltip');
        if (tooltips != null) {
            tooltips.forEach(t => {
                new mdc.tooltip.MDCTooltip(t);
            })
        }
        let banners = document.querySelectorAll('.mdc-banner');
        if (banners != null) {
            banners.forEach(b => {
                new mdc.banner.MDCBanner(b);
            })
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

    },
    checkCaps: ev => {
        let s = String.fromCharCode(ev.which);
        if (s.toUpperCase() === s && s.toLowerCase() !== s && !ev.shiftKey) {
            for (let el of document.getElementsByClassName("caps-warn")) {
                el.classList.remove("caps-warn");
                el.classList.add('caps-on');
            }
        } else {
            for (let el of document.getElementsByClassName("caps-on")) {
                el.classList.remove("caps-on");
                el.classList.add('caps-warn');
            }
        }

    }
};

document.addEventListener('DOMContentLoaded', () => cas.init());

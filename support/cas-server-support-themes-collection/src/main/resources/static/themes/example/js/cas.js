function resourceLoadedSuccessfully() {
    $(document).ready(function () {
        if ($(':focus').length === 0) {
            $('input:visible:enabled:first').focus();
        }
        if (typeof (jqueryReady) == 'function') {
            jqueryReady();
        }
    });
}

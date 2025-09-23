function resourceLoadedSuccessfully() {
    $(document).ready(() => {
        if ($(':focus').length === 0) {
            $('input:visible:enabled:first').focus();
        }
    });
}

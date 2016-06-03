<%@include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp"%>


<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/cupertino/jquery-ui.css">
<link rel="stylesheet" href="//cdn.datatables.net/plug-ins/1.10.6/integration/jqueryui/dataTables.jqueryui.css" />

<script type="text/javascript">
    function jqueryReady() {
        head.load(
            // Bootstrap Datables CSS
            "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css",
            "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css",
            "https://cdn.datatables.net/1.10.9/css/dataTables.bootstrap.min.css"
        );

        head.load(
            // JS Libraries
            "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js",

            // Bootstrap Datatables
            "https://cdn.datatables.net/1.10.9/js/jquery.dataTables.min.js",
            "https://cdn.datatables.net/1.10.9/js/dataTables.bootstrap.min.js",
            "/cas/js/viewConfig.js"
        );
    }
</script>

<div class="viewConfig">
    <div id="loadingMessage"><h3><spring:message code="cas.viewconfig.loading" /></h3></div>


    <div id="viewConfigError">
        <h2><spring:message code="cas.viewconfig.errormessage" /></h2>
        <div>
            <input class="btn btn-success" type="button" onclick="location.reload();" value="<spring:message code="cas.viewconfig.button.refresh" />">
        </div>
    </div>

    <div id="view-configuration">
        <div id="alertWrapper"></div>

        <div class="panel panel-default">
            <div class="panel-heading">
                <h4><span class="glyphicon glyphicon-cog" aria-hidden="true"></span> <spring:message code="cas.viewconfig.pagetitle" /></h4>
            </div>
            <div class="panel-body">
                <div id="containers-table" class="container-fluid">
                    <div id="msg" style="display:none"></div>
                    <table id="viewConfigsTable" class="display table table-striped table-bordered">
                        <thead>
                            <tr>
                                <th><spring:message code="cas.viewconfig.table.column.key" /></th>
                                <th><spring:message code="cas.viewconfig.table.column.value" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td></td>
                                <td></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

            </div>

        </div>
        <div id="login">
            <input class="btn-submit" type="button" onclick="location.reload();" value="<spring:message code="cas.viewconfig.button.refresh" />">
        </div>
    </div>

</div>
<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>

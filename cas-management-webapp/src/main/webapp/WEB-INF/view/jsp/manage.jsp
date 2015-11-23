<%@include file="includes/header.jsp" %>

        <!-- Content -->
        <div class="container-fluid casmgmt-content">

            <!-- Manage services content -->
            <div class="casmgmt-manage" ng-show="action.isSelected('manage')">
                <div class="row">
                    <div class="col-sm-12">
                        <h1><i class="fa fa-gears"></i> <spring:message code="management.services.header.navbar.navitem.manageService" /></h1>
                    </div> <!-- end .col-sm-12 div -->
                </div> <!-- end .row div -->

                <!-- Services table -->
                <%@include file="includes/services.jsp" %>

                <div class="row">
                    <div class="col-sm-12">
                        <button class="btn btn-info" ng-click="action.serviceAdd()">
                            <i class="fa fa-plus-circle"></i>
                            <spring:message code="management.services.header.navbar.navitem.addNewService" />
                        </button>
                    </div>
                </div>
            </div> <!-- end .casmgmt-manage div -->

            <!-- Add/edit services form -->
            <div class="casmgmt-form" ng-show="action.isSelected('add') || action.isSelected('edit')">
                <%@include file="includes/service-form.jsp" %>
            </div> <!-- end .casmgmt-form div -->

        </div> <!-- end .casmgmt-content div -->

        <!-- Footer -->
<%@include file="includes/footer.jsp" %>

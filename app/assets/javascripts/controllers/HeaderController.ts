/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export class HeaderController {
        public static NAME = "headerController";

        public static $inject = [
            '$scope',
            '$state',
            LoggedUserService.NAME,
            BackendService.NAME,
            ModelProvider.NAME
        ];

        constructor($scope:any, $state:ng.ui.IStateService, loggedUser:LoggedUserService, backendService:BackendService, modelProvider:ModelProvider) {
            $scope.user = loggedUser.getLoggedUser();
            $scope.logout = backendService.controllers.Login.logout().absoluteURL();

            $scope.config = ()=>$state.go('config');

            modelProvider.lastDevelopBuilds.then(builds=> {
                $scope.getLastBuild = ()=> builds[0];
                $scope.getPrevBuild = ()=> builds[1];
            });


        }
    }


}
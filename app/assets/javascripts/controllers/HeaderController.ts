/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export class HeaderController {
        public static NAME = "headerController";

        public static $inject = [
            '$scope',
            LoggedUserService.NAME,
            BackendService.NAME,
            ModelProvider.NAME
        ];

        constructor($scope:any, loggedUser:LoggedUserService, backendService:BackendService, modelProvider:ModelProvider) {
            $scope.user = loggedUser.getLoggedUser();
            $scope.logout = backendService.controllers.Login.logout().absoluteURL();

           // $scope.getLastBuild = () => modelProvider.getLastBuild('develop');

           // $scope.getPrevBuild = () => modelProvider.getPrevBuild('develop');
        }
    }


}
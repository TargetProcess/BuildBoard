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

            var $branch = modelProvider.findBranch('develop');

            $scope.getLastStatus = () => Status.Unknown;
            $branch.then(branch=>$scope.getLastStatus = () => StatusHelper.parse(branch.lastBuild));

        }
    }


}
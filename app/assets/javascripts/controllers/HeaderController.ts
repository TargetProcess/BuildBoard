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

            $scope.getLastStatus = () => {
                var branch = modelProvider.findBranch('develop');
                if (branch) {
                    return branch.lastBuild;
                }
            };

            $scope.getPrevStatus = () => {
                var branch = modelProvider.findBranch('develop');
                if (!branch || !branch.builds) {
                    return null;
                }

                var result = _.chain(branch.builds)
                    .find(x=> {
                        return x.getStatus() != Status.Unknown && x.getStatus() != Status.InProgress
                    })
                    .value();

                return result;
            };
        }
    }


}
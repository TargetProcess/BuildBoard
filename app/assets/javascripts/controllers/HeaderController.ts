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
                if (!branch || !branch.activity) {
                    return null;
                }

                var lastBuild = $scope.getLastStatus();

                return _.chain(branch.activity)
                    .find(x=> {
                        if (lastBuild && lastBuild.timestamp == x.timestamp)
                            return false;

                        if (x.activityType == "build" || x.parsedStatus) {

                            var status = (<BuildBase>(x)).parsedStatus;
                            return !(status == Status.Unknown || status == Status.InProgress);


                        }
                    })
                    .value();
            };
        }
    }


}
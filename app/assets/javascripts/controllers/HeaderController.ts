/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export class HeaderController {
        public static NAME = "headerController";

        public static $inject = [
            '$scope',
            LoggedUserService.NAME,
            BackendService.NAME
        ];


        findBuild (branches:Branch[], branchName:string, buildFinder:(b:Branch)=>BuildInfo) {
            var branch = _.find(branches, br=>br.name == branchName);
            if (branch){
                return StatusHelper.parse(buildFinder(branch));
            }
            else {
                return Status.Unknown;
            }
        }

        constructor($scope:any, loggedUser:LoggedUserService, backendService:BackendService) {
            $scope.user = loggedUser.getLoggedUser();
            $scope.logout = backendService.controllers.Login.logout().absoluteURL();
            var parentScope = <IBranchesScope>$scope.$parent;
            $scope.getLastStatus = (branchName:string) => this.findBuild(parentScope.allBranches, branchName, branch=> branch.lastBuild);
        }
    }


}
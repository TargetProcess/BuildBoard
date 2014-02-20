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


        findBuild (branches:Branch[], branchName:string, buildFinder:(b:Branch)=>Build) {
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
            $scope.getLastStatus = (branchName:string)=>this.findBuild($scope.$parent.allBranches, branchName, branch=>branch.lastBuild);
        }
    }


}
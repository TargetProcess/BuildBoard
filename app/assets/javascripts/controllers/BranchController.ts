/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchRouteParams extends ng.IRouteParamsService {
        branchType:string;
        branchId:string;
    }

    export interface IBranchScope extends ng.IScope {
        branchName:string;
        builds: Build[];
    }


    export class BranchController {
        public static $inject = [
            '$scope',
            '$routeParams',
            BackendService.NAME
        ];

        constructor(private $scope:IBranchScope, $routeParams:IBranchRouteParams, backendService:BackendService) {
            var branchType = $routeParams.branchType;
            var branchId = $routeParams.branchId;

            this.$scope.branchName = (branchId && branchType) ? branchType + '/' + branchId : (branchId || branchType);

            backendService.branch(this.$scope.branchName).success(branch=> {
                this.$scope.branch = branch;
            });

            backendService.builds(this.$scope.branchName).success(builds=> {
                this.$scope.builds = builds;
            });
        }
    }


}
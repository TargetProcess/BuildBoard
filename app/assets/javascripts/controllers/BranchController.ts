/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchRouteParams extends ng.IRouteParamsService {
        branchType:string;
        branchId:string;
    }


    export class BranchController extends BranchControllerBase {
        public static $inject = [
            '$scope',
            '$routeParams',
            BackendService.NAME
        ];

        constructor($scope:IBranchScope, $routeParams:IBranchRouteParams, backendService:BackendService) {
            super($scope, backendService);

            var branchType = $routeParams.branchType;
            var branchId = $routeParams.branchId;

            this.$scope.branchName = (branchId && branchType) ? branchType + '/' + branchId : (branchId || branchType);

            backendService.branch(this.$scope.branchName).success(branch=> {
                this.$scope.branch = branch;
                this.loadPullRequestStatus(this.$scope.branch);
            });

            backendService.builds(this.$scope.branchName).success(builds=> {
                this.$scope.builds = builds;
            });
        }
    }


}
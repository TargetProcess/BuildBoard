/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';



    export class BranchLineController extends BranchControllerBase {
        public static NAME = 'branchLineController';


        public static $inject = [
            '$scope',
            BackendService.NAME
        ];

        constructor($scope:IBranchScope, backendService:BackendService) {
            super($scope, backendService);

            this.loadPullRequestStatus(this.$scope.branch);
        }

    }
}
/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';



    export class BranchLineController extends BranchControllerBase {
        public static NAME = 'branchLineController';


        public static $inject = [
            '$scope',
            BackendService.NAME,
            ModelProvider.NAME
        ];

        constructor($scope:IBranchScope, backendService:BackendService, modelProvider:ModelProvider) {
            super($scope, backendService, modelProvider);

            this.loadPullRequestStatus(this.$scope.branchName);
        }

    }
}
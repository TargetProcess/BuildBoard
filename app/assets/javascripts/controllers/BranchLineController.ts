/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchLineScope extends IBranchScope {
        branch:Branch;
    }


    export class BranchLineController extends BranchControllerBase {
        public static NAME = 'branchLineController';


        public static $inject = [
            '$scope',
            BackendService.NAME,
            ModelProvider.NAME
        ];

        constructor($scope:IBranchLineScope, backendService:BackendService, modelProvider:ModelProvider) {
            super($scope, backendService, modelProvider);

            $scope.getBranch = ()=>$scope.branch;
        }
    }
}
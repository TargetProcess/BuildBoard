/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchLineScope extends IBranchScope {
        branch:Branch;
    }


    export class BranchLineController {
        public static NAME = 'branchLineController';


        public static $inject = [
            '$scope'
        ];

        constructor(private $scope:IBranchLineScope) {

            //$scope.getBranch = ()=>$scope.branch;
        }
    }
}
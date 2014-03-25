/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchScope extends ng.IScope {
        branchName:string;
        getBranch(): Branch;
        closeView():void;
        mergeStatus():MergeStatus;
        isPossibleToMerge():boolean;
        merge():void;
    }

    export interface MergeStatus{
        isEnabled:boolean;
        reasons:string[];
    }

    export class BranchControllerBase {
        constructor(public $scope:IBranchScope, public backendService:BackendService, private modelProvider:ModelProvider) {
            this.$scope.getBranch = () => modelProvider.findBranch(this.$scope.branchName);
       }
    }
}
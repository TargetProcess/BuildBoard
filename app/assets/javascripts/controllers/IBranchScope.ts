/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchScope extends ng.IScope {
        branchName:string;
        branch: Branch;
        closeView():void;
        mergeStatus():MergeStatus;
        isPossibleToMerge():boolean;
        merge():void;
    }

    export interface MergeStatus {
        isEnabled:boolean;
        reasons:string[];
    }
}
/// <reference path='../_all.ts' />

module buildBoard {
    export interface IBranchesScope extends ng.IScope {
        isShowingAll : boolean;
        isShowingEntity: boolean;
        isShowingId:number;
        branches:Branch[];
        allBranches:Branch[];
        entityBranches:Branch[];
        predicate:string;
        entityBranchesLength:number;
        users:User[];
        branchCount(id:number):number;
        filterBranch(id:number):void;
        resetFilterBranch():void;
        filterOnlyEntityBranch():void;
        loading:boolean;
    }
}
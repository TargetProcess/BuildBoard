/// <reference path='../_all.ts' />

module buildBoard {
    export interface IBranchesScope extends ng.IScope {
        allBranches:Branch[];
        users:User[];

        getBuildClass(branch:Branch)

        loading:boolean;
    }

}
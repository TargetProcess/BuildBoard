/// <reference path='../_all.ts' />

module buildBoard {
    export interface IBranchesScope extends ng.IScope {

        currentFilter : IFilter

        setFilter(filter:IFilter);
        checkCurrentFilter(filter:IFilter);

        allBranches:Branch[];
        users:User[];

        allBranchesFilter:IFilter
        entityBranchesFilter:IFilter
        getUserFilter(userId:number):IFilter

        getBuildClass(branch:Branch)


        loading:boolean;
    }

}
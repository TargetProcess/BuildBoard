/// <reference path='../_all.ts' />

module buildBoard {
    export interface IBuildBoardRouteParamsService extends ng.IRouteParamsService {
        allBranches:boolean
        entityBranches:boolean
        myBranches:boolean
        closedBranches:boolean
        userId:number
    }
}

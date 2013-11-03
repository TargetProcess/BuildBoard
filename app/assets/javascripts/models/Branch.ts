/// <reference path='../_all.ts' />


// loaded from server

module buildBoard {
    export interface Branch {
        entity:Entity
        pullRequest:PullRequest
    }

    export interface PullRequest{
        id:number;
    }

    export interface Entity {
        assignmentsOpt:Assignment[]
    }


    export interface Assignment {
        userId:number
    }

    export interface PRStatus {
        isMerged:boolean;
        isMergeable:boolean;
    }
}
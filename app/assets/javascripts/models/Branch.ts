/// <reference path='../_all.ts' />


// loaded from server

module buildBoard {
    export interface Branch {
        name:string
        entity:Entity
        pullRequest:PullRequest
        lastBuild:Build
        url:string;
    }

    export interface PullRequest {
        id:number;
        status:PRStatus;
        url:string;
    }

    export interface Entity {
        id: number
        assignments:Assignment[]
        state: EntityState
    }

    export interface EntityState {
        isClosed : boolean
    }

    export interface Assignment {
        userId:number
        isResponsible:boolean
    }

    export interface PRStatus {
        isMerged:boolean;
        isMergeable:boolean;
    }

    export class Build {
        timeStamp:number;
        status:string;
        branch:string;
    }

    export class BuildAction {
        branchId:string;
        pullRequestId:number;
        fullCycle:boolean;
    }
}
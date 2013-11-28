/// <reference path='../_all.ts' />


// loaded from server

module buildBoard {
    export interface Branch {
        name:string
        entity:Entity
        pullRequest:PullRequest
        lastBuild:Build

    }

    export interface PullRequest {
        id:number;
        status:PRStatus;
    }

    export interface Entity {
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
        timeStamp:string;
        status:string;
    }
}
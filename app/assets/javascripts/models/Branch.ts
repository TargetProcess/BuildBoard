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
        prId:number;
        status:PRStatus;
        url:string;
        created:number;
    }

    export interface Entity {
        id: number
        assignments:Assignment[]
        state: EntityState
    }

    export interface EntityState {
        name:string
        isFinal : boolean
    }

    export interface User {
        userId:number
    }

    export interface Assignment extends User{
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

    export class ToggledBuild {
        branchId:string;
        buildNumber:number;
    }
}
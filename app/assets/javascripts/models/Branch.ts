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
        number:number;
        timeStamp:number;
        status:string;
        branch:string;
        toggled:boolean;
        node: BuildNode;
    }

    export class BuildNode {
        name: string;
        runName: string;
        status: string;
        statusUrl: string;
        artifactsUrl: string;
        timestamp: number;
        children: BuildNode[];
        testCasePackage: TestCasePackage;
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

    export class TestCase {
        name:string;
        executed:boolean;
        result:string;
        duration:number;
        message:string;
        stackTrace:string;
    }

    export class TestCasePackage {
        name:string;
        packages:TestCasePackage[];
        testCases:TestCase[];
    }
}
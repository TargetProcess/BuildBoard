/// <reference path='../_all.ts' />


// loaded from server

module buildBoard {

    export class ActivityEntry {
        timestamp:number;
    }

    export class Branch {
        _id:number;
        name:string;
        entity:Entity;
        pullRequest:PullRequest;
        lastBuild:BuildInfo;
        url:string;
        builds:BuildInfo[];
        activity:ActivityEntry[];
    }

    export class PullRequest extends ActivityEntry {
        prId:number;
        status:PullRequestStatus;
        url:string;
        created:number;
    }

    export class Entity {
        id:number;
        assignments:Assignment[];
        state:EntityState;
    }

    export class EntityState {
        name:string;
        isFinal:boolean;
    }

    export class User {
        userId:number;
    }

    export class Assignment extends User {
        isResponsible:boolean;
    }

    export class PullRequestStatus {
        isMerged:boolean;
        isMergeable:boolean;
    }

    export class BuildBase extends ActivityEntry {
        number:number;
        branch:string;
        toggled:boolean;
        status:string;

        getStatus():Status {
            throw new Error('This method is abstract');
        }
    }

    export class BuildInfo extends BuildBase {
        node:BuildNode;
    }

    export class Build extends BuildBase {
        isPullRequest:boolean;
    }

    export class Artifact {
        name:string;
        url:string;
    }

    export class BuildNode {
        name:string;
        runName:string;
        status:string;
        statusUrl:string;
        artifacts:Artifact[];
        timestamp:number;
        children:BuildNode[];
        testResults:TestCasePackage[];
    }

    export class BuildAction {
        branchId:string;
        pullRequestId:number;
        cycleName:string;
    }

    export class ToggledBuild {
        branchId:string;
        buildNumber:number;
    }

    export class TestCase {
        name:string;
        result:string;
        duration:number;
        message:string;
        stackTrace:string;
        screenshots:Artifact[];
    }

    export class TestCasePackage {
        name:string;
        packages:TestCasePackage[];
        testCases:TestCase[];
        totalCount:number;
        passedCount:number;
        skippedCount:number;
        failedCount:number;
        duration:number;
    }

    export enum Status {
        Failed,
        Toggled,
        Success,
        InProgress,
        Unknown,
        Aborted,
        TimedOut
    }

    export class StatusHelper {
        static parse(build:BuildInfo):Status {
            return build ? StatusHelper.parseInfo(build.status, build.toggled) : Status.Unknown;
        }

        static parseBuildNode(node:BuildNode):Status {
            return node ? StatusHelper.parseInfo(node.status) : Status.Unknown;
        }

        static parseTestCase(testCase:TestCase):Status {
            return testCase ? StatusHelper.parseInfo(testCase.result) : Status.Unknown;
        }

        static parseInfo(status:string, toggled?:boolean):Status {
            if (toggled)
                return Status.Toggled;

            if (!status)
                return Status.InProgress;


            switch (status.toString().toLowerCase()) {
                case 'in progress':
                    return Status.InProgress;
                case 'fail':
                case 'failed':
                case 'failure':
                    return Status.Failed;
                case 'success':
                case 'ok':
                    return Status.Success;
                case 'aborted':
                    return Status.Aborted;
                case 'timed out':
                    return Status.TimedOut;
                default:
                    return Status.Unknown;
            }
        }
    }
}
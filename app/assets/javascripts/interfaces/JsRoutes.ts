module buildBoard {
    export interface IAction {
        absoluteURL(): string
    }

    export interface JsRoutes {
        controllers: Controllers
    }

    export interface Controllers {
        Application : IApplication;
        Github: IGithub
        Jenkins: IJenkins
    }

    export interface IJenkins {
        builds(branch:string):IAction
        lastBuildInfos():IAction
        forceBuild(prId:number, branchId:string, fullCycle:boolean):IAction
    }

    export interface IGithub {
        pullRequestStatus(id:number):IAction
    }


    export interface IApplication {
        branches(): IAction;
        branch(id:string): IAction;
    }

}
module buildBoard {
    export interface IAction {
        absoluteURL(): string
    }

    export interface JsRoutes {
        controllers: Controllers
    }

    export interface Controllers {
        Login : ILogin;
        Application : IApplication;
        Github: IGithub
        Jenkins: IJenkins
        Targetprocess: ITargetProcess
    }

    export interface ITargetProcess{
        changeEntityState(entityId:number, nextStateId:number)
    }

    export interface IJenkins {
        forceBuild(prId:number, branchId:string, fullCycle:boolean):IAction
    }

    export interface IGithub {
        pullRequestStatus(id:number):IAction
    }


    export interface IApplication {
        lastBuildInfos():IAction
        branches(): IAction;
        branch(id:string): IAction;
        builds(branch:string):IAction
    }

    export interface ILogin {
        logout():IAction
    }

}
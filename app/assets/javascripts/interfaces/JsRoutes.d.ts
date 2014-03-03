declare module buildBoard {
    interface IAction {
        absoluteURL(): string
    }

    interface JsRoutes {
        controllers: Controllers
    }

    interface Controllers {
        Login : ILogin;
        Application : IApplication;
        Github: IGithub
        Jenkins: IJenkins
        Targetprocess: ITargetProcess
    }

    interface ITargetProcess {
        changeEntityState(entityId:number, nextStateId:number)
    }

    interface IJenkins {
        forceBuild(prId:number, branchId:string, cycleName:string):IAction
        toggleBuild(branchId:string, buildNumber:number):IAction
        lastBuildInfos():IAction;
        builds(branch:string):IAction;
        build(branch:string, buildNumber :number);
        run(branch:string, build: number, part: string, run: string):IAction
        testCase(branch:string, build: number, part: string, run: string, test: string):IAction
    }

    interface IGithub {
        pullRequestStatus(id:number):IAction
    }


    interface IApplication {
        branches(): IAction;
        branch(id:string): IAction;
    }

    export interface ILogin {
        logout():IAction
    }

}
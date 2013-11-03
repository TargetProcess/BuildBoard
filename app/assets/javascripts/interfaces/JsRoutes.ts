module buildBoard {
    export interface JsRoutes {
        controllers: Controllers
    }

    export interface Controllers {
        Application : IApplication;
        Github: IGithub
    }

    export interface IGithub {
        pullRequestStatus(id:number):IAction
    }

    export interface IAction {
        absoluteURL(): string
    }

    export interface IApplication {
        branches(): IAction
    }

}
/// <reference path='../_all.ts' />
module buildBoard {

    export class LoggedUserService {
        public static NAME = "loggedUser";

        private loggedUser:User;

        public static $inject = [
            '$window'
        ];

        constructor($window:ng.IWindowService) {
            this.loggedUser = $window["loggedUser"];
        }

        public getLoggedUser():User {
            return this.loggedUser;
        }

    }

}
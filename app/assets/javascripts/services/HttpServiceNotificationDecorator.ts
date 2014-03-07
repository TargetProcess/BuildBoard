/// <reference path='../_all.ts' />
module buildBoard {
    export class HttpServiceNotificationDecorator {
        public static NAME = "httpNotifiable";

        public static $inject = [
            '$http'
        ];

        private counter = 0;

        private statusChangeHandlers:{(status:String): void;}[] = [];

        constructor(private $http:ng.IHttpService) {
        }

        get(url:string, RequestConfig?:any):ng.IHttpPromise<any> {
            this.beginRequest();

            return this.$http.get(url, RequestConfig)
                .success(result => {
                    this.endRequest();

                    return result;
                })
                .error(result => {
                    this.endRequest();

                    return result;
                });
        }

        post(url:string, RequestConfig?:any):ng.IHttpPromise<any> {
            this.beginRequest();

            return this.$http.post(url, RequestConfig)
                .success(result => {
                    this.endRequest();

                    return result;
                })
                .error(result => {
                    this.endRequest();

                    return result;
                });
        }

        private beginRequest():void {
            var counter = ++this.counter;
            if (counter == 1) {
                var status = this.status(counter);
                _.each(this.statusChangeHandlers, function (handler) {
                    handler(status);
                });
            }
        }

        private endRequest():void {
            var counter = --this.counter;
            if (counter == 0) {
                var status = this.status(counter);
                _.each(this.statusChangeHandlers, function (handler) {
                    handler(status);
                });
            }
        }

        private status(counter:number):String {
            return counter == 0 ? '' : 'Loading';
        }

        addStatusChangedHandler(handler:(status:String) => void):void {
            this.statusChangeHandlers.push(handler);
        }
    }
}

/// <reference path='../_all.ts' />
module buildBoard {

    var statusMap:{ [s: string]: string;
    } = {
        "aborted" : "warning",
        "failure": "warning",
        "unstable": "warning",
        "finished": "success",
        "success": "success",
        "toggled": "success",
        "ok": "success"
    };

    export function activeFilter() {
        return (isActive:boolean)=>isActive ? 'active' : '';
    }

    export function encode() {
        return encodeURIComponent;
    }

    export function status() {
        return (status:string)=>status ? (statusMap[status.toLowerCase()] || status.toLowerCase()) : 'default';
    }

    export function pullRequestStatus(){
        return (pullRequest:PullRequest)=>{
                if (pullRequest && pullRequest.status) {
                    if (pullRequest.status.isMerged) {
                        return 'success';
                    } else if (pullRequest.status.isMergeable) {
                        return 'success';
                    } else {
                        return 'warning';
                    }
                }
                else {
                    return '';
                }
        }
    }

    export function duration() {
        return (seconds: number) => {
            var result = '';

            var hours = (seconds / 3600) / 1;
            if (hours > 1){
                result = Math.round(hours) + 'h ';
            }

            var minutes = (seconds % 3600) / 60;
            if (hours > 1 || minutes > 1){
                result = result + Math.round(minutes) + "m ";
            }

            var seconds = (seconds % 60) / 1;
            result = result + Math.round(seconds) + 's';

            return result;
        }
    }
}

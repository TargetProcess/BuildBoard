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
}

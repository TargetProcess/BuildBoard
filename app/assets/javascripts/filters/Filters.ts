/// <reference path='../_all.ts' />
module buildBoard {

    var statusMap:{ [s: string]: string;
    } = {
        "failure": "danger",
        "finished": "primary",
        "success": "success",
        "ok": "success"
    };

    export function activeFilter() {
        return (isActive:boolean)=>isActive ? 'active' : '';
    }

    export function encode() {
        return encodeURIComponent;
    }

    export function status() {
        return (status:string)=>((status && statusMap[status.toLowerCase()]) || "default");
    }
}

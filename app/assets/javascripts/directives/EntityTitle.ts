/// <reference path="../_all.ts" />
module buildBoard {
    export class EntityTitleDirective implements ng.IDirective {

        static name = "entityTitle";


        scope = {
            entity: "=",
            url: "@"
        };


        template = [
            '<div ng-show="entity">',
            '<a href="{{entity.url}}" target="_blank" class="ui-entity ui-entity-{{entity.entityType.toLowerCase()}}">{{entity.id}}</a>',
            '<a href="{{url}}">',
            '{{entity.name}}',
            '</a>',
            '</div>'
        ].join("");

        restrict = "EA";
        replace = true;
    }
}
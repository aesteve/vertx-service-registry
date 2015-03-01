var _ = require('underscore');
var $ = require('jquery');

var ServicesCollection = function(){
    this.currentPageServices = {};
    this.pagination = {
        current:1,
        next:undefined,
        prev:undefined,
        first:undefined,
        last:undefined
    };
};

ServicesCollection.prototype.fetch = function(){
    var def = new $.Deferred();
    var instance_ = this;
    $.ajax("/api/1/services/", {
        beforeSend:function(xhr){
            var filters = instance_.filters;
            if (!filters)
                return;
            if (instance_.filters.textSearch) {
                xhr.setRequestHeader("q",encodeURIComponent(instance_.filters.textSearch));
            }
            var tags = instance_.filters.tags;
            if (tags) {
                xhr.setRequestHeader("tags", encodeURIComponent(tags.join(",")));
            }
        },
        method:"GET",
        dataType:"json",
        contentType:"application/json",
        success:function(data, status, xhr){
            instance_.parseLinkHeader(xhr.getResponseHeader("Link"));
            instance_.currentPageServices = data;
            def.resolve();
        },
        error:function(jqXhr, statusMsg, error){
            console.error(statusMsg);
        }
    });
    return def;
};

ServicesCollection.prototype.parseLinkHeader = function(header){
    if (header.length == 0) {
        throw new Error("input must not be of zero length");
    }

    var parts = header.split(',');
    var links = {};
    _.each(parts, function(p) {
        var section = p.split(';');
        if (section.length != 2) {
            throw new Error("section could not be split on ';'");
        }
        var url = section[0].replace(/<(.*)>/, '$1').trim();
        var name = section[1].replace(/rel="(.*)"/, '$1').trim();
        links[name] = url;
    });

    var currentPage = this.pagination.current;
    this.pagination = links;
    this.pagination.current = currentPage;
};

module.exports = ServicesCollection;
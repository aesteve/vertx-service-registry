var _ = require('underscore');
var $ = require('jquery');

var ResourcesCollection = function(apiVersion, apiResource){
    this.apiVersion = apiVersion;
    this.apiResource = apiResource;
    this.reset();
};

ResourcesCollection.prototype.fetch = function(url){
    var def = new $.Deferred();
    var instance_ = this;
    var getQueryParams = function(){
        var params = {};
        var filters = instance_.filters;
        if (filters) {
            if (filters.textSearch) {
                params['q'] = encodeURIComponent(filters.textSearch);
            }
            var tags = filters.tags;
            if (tags && tags.length > 0) {
                params['tags'] = encodeURIComponent(tags.join(","));
            }
        }
        params.page=instance_.pagination.current;
        params.perPage=30;
        return params;
    };
    var url = "/api/"+this.apiVersion+"/"+this.apiResource+"/";
    $.ajax(url, {
        data:getQueryParams(),
        method:"GET",
        dataType:"json",
        contentType:"application/json",
        success:function(data, status, xhr){
            instance_.parseLinkHeader(xhr.getResponseHeader("Link"));
            instance_.currentPageResources = data;
            def.resolve();
        },
        error:function(jqXhr, statusMsg, error){
            console.error(statusMsg);
            def.reject(error);
        }
    });
    return def;
};

ResourcesCollection.prototype.setApiVersion = function(apiVersion){
    this.apiVersion = apiVersion;
};

ResourcesCollection.prototype.parseLinkHeader = function(header){
    if (!header || header.length == 0) {
        this.pagination.current = 1;
        this.pagination.next = undefined;
        this.pagination.prev = undefined;
        this.pagination.last = undefined;
        this.pagination.first = undefined;
        
        return;
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
    if (this.pagination.last){
        console.log("set pagination total : "+this.getParamValue(this.pagination.last, "page"));
        this.pagination.total = this.getParamValue(this.pagination.last, "page");
    } else {
        this.pagination.total = 1;
    }
};

ResourcesCollection.prototype.goTo = function(rel){
    var url = this.pagination[rel];
    var page = this.getParamValue(url, "page");
    this.pagination.current = page;
    return this.fetch();
};

ResourcesCollection.prototype.getParamValue = function(url, name){
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
    results = regex.exec(url);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
};

ResourcesCollection.prototype.setCurrentFromUrl = function(){
    this.pagination.current = this.getParamValue(document.location.toString(), "page") || 1 ;
};

ResourcesCollection.prototype.reset = function(){
    this.pagination = {
        current:1,
        next:undefined,
        prev:undefined,
        first:undefined,
        last:undefined
    };
    this.filters = {};
    this.currentPageResources = {};
};

module.exports = ResourcesCollection;
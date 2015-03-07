var $ = require('jquery');
var React = require('react');


var factory = require('./MainPage');

var initialProps = {};
var getParamValue = function(url, name){
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
    results = regex.exec(url);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
};
var textSearch = getParamValue(document.location.toString(), "q");
initialProps.filters = {textSearch:textSearch};
var instance = factory(initialProps);
$(document).ready(function(){
    React.render(instance, document.getElementById("content"));
});

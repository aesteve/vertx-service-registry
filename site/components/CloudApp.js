var $ = require('jquery');
var React = require('react');


var factory = require('./CloudPage');

var initialProps = {};
var instance = factory(initialProps);
$(document).ready(function(){
    React.render(instance, document.getElementById("content"));
});

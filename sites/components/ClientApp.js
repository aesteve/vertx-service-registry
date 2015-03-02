var $ = require('jquery');
var React = require('react');


var factory = require('./MainPage');

var initialProps = {};
var instance = factory(initialProps);

React.render(instance, document.getElementById("content"));
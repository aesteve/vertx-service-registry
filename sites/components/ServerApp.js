var React = require('react');

var factory = require('./MainPage');
var instance = factory(initialProps);

reactRenderedResult = React.renderToString(instance);
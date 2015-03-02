var React = require('react');

var factory = require('./MainPage');
var initialProps = {};
process = {env:{}};
initialProps.services = JSON.parse(nashorn_services.encode());
initialProps.renderedOnServer = true;
var instance = factory(initialProps);

reactRenderedResult = React.renderToString(instance);
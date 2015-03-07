var React = require('react');

var factory = require('./MainPage');
var initialProps = {};
process = {env:{}};
initialProps.services = JSON.parse(nashorn_services.encode());
initialProps.renderedOnServer = true;
initialProps.pagination = JSON.parse(paginationContext.encode());
var instance = factory(initialProps);

reactRenderedResult = React.renderToStaticMarkup(instance);
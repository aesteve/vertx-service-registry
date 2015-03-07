var React = require('react');

var Header = React.createClass({
	render: function(){
        return (
            <header>
                <nav><a href="http://vertx.io"><img className="vertx-logo" src="/assets/img/vertx-logo.png" /></a></nav>
            </header>
        );
	}
});

module.exports = Header;
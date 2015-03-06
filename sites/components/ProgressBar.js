var React = require('react');

var ProgressBar = React.createClass({
	render: function(){
		return (
			<div>{this.props.current}/{this.props.total}<div>
		);
	}
});

module.exports = ProgressBar;
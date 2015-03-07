var React = require('react');

var ProgressBar = React.createClass({
	render: function(){
		return (
			<progress className="task-progress" value={this.props.current} max={this.props.total}></progress>
		);
	}
});

module.exports = ProgressBar;
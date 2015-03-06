var React = require('react');
var moment = require('moment');

var Duration = React.createClass({
	render: function(){
		return (
			<span>
				<span className="report-icon hourglass-icon octicon octicon-clock"></span>
				<span className="timestamp">{moment.duration(this.props.time).seconds()} seconds</span>
			</span>
		);
	}
});

module.exports = Duration;
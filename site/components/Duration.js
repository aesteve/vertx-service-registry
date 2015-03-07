var React = require('react');
var moment = require('moment');
require('moment-duration-format');
var Loader = require('./Loader');

var Duration = React.createClass({
	render: function(){
		var time = this.props.time;
		if (time < 0) {
			return (<span></span>);
		} else {
			console.log(time);
			return (
				<span>
					<span className="report-icon hourglass-icon octicon octicon-clock"></span>
					<span className="timestamp">{moment.duration(time).format('m [min] s [seconds]')}</span>
				</span>
			);
		}
	}
});

module.exports = Duration;
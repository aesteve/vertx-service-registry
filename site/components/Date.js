var React = require('react');
var moment = require('moment');

var Date = React.createClass({
    render: function(){
        return <span className="date">{this.format()}</span>
    },
    format: function(){
    	var date = moment(this.props.timestamp);
    	if (this.props.humanize){
    		return date.fromNow();
    	} 
        return date.format('DD/MM/YYYY, HH:mm:ss');
    }
});

module.exports = Date;
var React = require('react');
var moment = require('moment');

var Date = React.createClass({
    render: function(){
        return <span className="date">{this.format()}</span>
    },
    format: function(){
        return moment(this.props.timestamp).format('DD/MM/YYYY, HH:mm:ss');
    }
});

module.exports = Date;
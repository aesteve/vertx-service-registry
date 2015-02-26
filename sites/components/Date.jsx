var Date = React.createClass({
    render: function(){
        return <span className="date">{this.format()}</span>
    },
    format: function(){
        return this.props.timestamp; // FIXME : use moment ?
    }
});

module.exports = Date;
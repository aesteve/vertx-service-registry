var React = require('react');
var Version = React.createClass({
    render:function(){
        return (
            <li className="version">{this.props.name}</li>
        );
    }
});

module.exports = Version;
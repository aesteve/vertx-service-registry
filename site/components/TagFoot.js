var React = require('react');
var TagFoot = React.createClass({
    render: function(){
        return (
            <li className="tag">{this.props.tag}</li>
        );
    }
});

module.exports = TagFoot;
var React = require('react');
var Tag = React.createClass({
    render: function(){
        // boilerPlate because no "getInitialState"...
        // should be in getInitialState, but not working with nashorn
        if(!this.state)
            this.state = {};
        
        return (
           <span className="tag">{this.props.tag.name}</span>
        );
    }
});

module.exports = Tag;
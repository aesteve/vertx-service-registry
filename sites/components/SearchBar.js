var React = require('react');
var Tag = require('./Tag');
var _ = require('underscore');

var SearchBar = React.createClass({
	render: function(){
        // should be in getInitialState, but not working with nashorn
        if(!this.state)
            this.state = {};
        
        var filterTags = this.state.tags || this.props.tags;
        var tags = _.map(filterTags, function(tag){
            return (<Tag tag={tag} />);
        });
		return (
            <div className="search-bar">
                <span className="search">
                    <input className="text-field" type="text" placeholder="Search service" />
                </span>
                {filterTags && <span className="filters">
                    Filters: {tags}
                </span>}
                <a href="javascript:void(0)">advanced</a>
                <div className="reset"></div>
            </div>
        );
	}
});

module.exports = SearchBar
var React = require('react');
var Tag = require('./Tag');
var _ = require('underscore');

var SearchBar = React.createClass({
    getInitialState: function(){
        return {
            filters:this.props.filters,
            advanced:false
        };
    },
	render: function(){
        var filters = this.state.filters;
        var tags = _.map(filters.tags, function(tag){
            return (<Tag tag={tag} />);
        });
		return (
            <div className="search-bar">
                <span className="search">
                    <input className="text-field" type="text" value={filters.textSearch} onChange={this.textSearchChanged} placeholder="Search service" />
                </span>
                {tags && <span className="filters">
                    Filters: {tags}
                </span>}
                <a href="javascript:void(0)" onClick={this.toggleAdvancedSearch}>advanced</a>
                <div className="reset"></div>
            </div>
        );
	},
    textSearchChanged: function(event){
        // Since React doesn't handle nested properties in setState, we have to copy it
        var newFilters = _.extend({}, this.state.filters);
        newFilters.textSearch = event.target.value;
        this.setState({filters:newFilters});
        console.log(this.state.filters.textSearch);
        this.props.filtersChanged(this.state.filters);
    },
    toggleAdvancedSearch: function(){
        this.setState({advanced:!this.state.advanced});
    }
});

module.exports = SearchBar
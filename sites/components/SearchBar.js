var React = require('react');
var Tag = require('./Tag');
var _ = require('underscore');

var SearchBar = React.createClass({
    getInitialState: function(){
        return {
            filters:this.props.filters,
            advanced:false,
            apiVersion:this.props.apiVersion
        };
    },
    componentWillMount: function(){
        var instance_ = this;
        this.delayed = _.debounce(function(event){
            instance_.props.filtersChanged(instance_.state.filters);
        }, 200);
    },
	render: function(){
        var filters = this.state.filters;
        var tags = _.map(filters.tags, function(tag){
            return (<Tag tag={tag} />);
        });
        var isChecked = (this.state.apiVersion == 2);
        var displayTags = tags && tags.length > 0;
		return (
            <div className="search-bar" id="search-bar">
                <span className="search">
                    <input className="text-field" type="text" value={filters.textSearch} onChange={this.textSearchChanged} placeholder="Search service" />
                </span>
                {displayTags && <span className="filters">
                    Filters: {tags}
                </span>}
                <a href="javascript:void(0)" onClick={this.toggleAdvancedSearch}>advanced</a>
                <div className="onoffswitch right">
                    <input type="checkbox" name="onoffswitch" className="onoffswitch-checkbox" id="myonoffswitch" onChange={this.apiVersionChanged} checked={isChecked} />
                    <label className="onoffswitch-label" htmlFor="myonoffswitch">
                        <span className="onoffswitch-inner"></span>
                        <span className="onoffswitch-switch"></span>
                    </label>
                </div>
                <div className="reset"></div>
            </div>
        );
	},
    textSearchChanged: function(event){
        // Since React doesn't handle nested properties in setState, we have to copy it
        var newFilters = _.extend({}, this.state.filters);
        newFilters.textSearch = event.target.value;
        this.setState({filters:newFilters}, this.delayed);
    },
    apiVersionChanged:function(){
        var newApiVersion;
        if (this.state.apiVersion == 2){
            newApiVersion = 3;
        } else {
            newApiVersion = 2;
        }
        var instance_ = this;
        this.setState({apiVersion: newApiVersion, filters:this.defaultFilters()}, function(){
            instance_.props.apiVersionChanged(newApiVersion);
        });
    },
    defaultFilters: function(){
        return {
            textSearch:"",
            tags:[]
        };
    },
    toggleAdvancedSearch: function(){
        this.setState({advanced:!this.state.advanced});
    }
});

module.exports = SearchBar
var Tag = require('Tag');

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
                    <input type="text" placeholder="Search service" />
                </span>
                <span className="filters">
                    Filters: {tags}
                </span>
                <span className="counters">
                    {this.props.nbMatchingServices}/{this.props.nbTotalServices} services
                </span>
                <div className="reset"></div>
            </div>
        );
	}
});

module.exports = SearchBar
var Header = require('Header');
var SearchBar = require('SearchBar');
var Services = require('Services');


var App = React.createClass({
	render: function () {
        // should be in getInitialState, but not working with nashorn
        if(!this.state)
            this.state = {};
        var services = this.state.matchingServices || this.props.matchingServices || this.props.services;
        var textSearch = "";
        if (this.state.filters && this.state.filters.text) 
            textSearch = this.state.filters.text;
        var sort;
        if (this.state.filters && this.state.filters.sort)
            sort = this.state.filters.sort;
		return (
            <div className="webapp">
                <Header />
                <SearchBar />
                <Services services={services} />
            </div>
        );
	}
});

module.exports = App
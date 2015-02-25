/*
var Tags = React.createClass({
	render: function(){
		return (
			<span className="tag" click="this.removeTagFromSearch">tag</span>
		);
	},
	removeTagFromSearch: function(){
	}
});
*/

var SearchBar = React.createClass({
	render: function(){
		return React.DOM.div(
            {className:"search-bar"},
            React.DOM.span(
                {className:"search"},
                React.DOM.input(
                    {type:"text",placeholder:"Search service"}
                ),
                React.DOM.span(
                    {className:"filters"},
                    "Filters : (tags)"
                ),
                React.DOM.span(
                    {className:"counters"},
                    "5/10 services"
                ),
                React.DOM.div({className:"reset"},"")
            )
        );
	},
	updateMatchingServices: function(){
		console.log("TODO");
	}
});

var Service = React.createClass({
    render: function(){
        return React.DOM.div({className:"service"}, "service");
    }
});

var Services = React.createClass({
	render: function(){
        var services = _.map(this.props.services, function(service){
            return Service({service:service});
        });
		return React.DOM.div({className:"services"}, services);
	}
});


var Header = React.createClass({
	render: function(){
		return React.DOM.h1({}, "Services registry");
	}
});



var FullApp = React.createClass({
	render: function(){
		return React.DOM.div(
            null,
            Header({}),
            SearchBar({}),
            Services({services:this.props.services})
        );
	}
});

/*
$(document).ready(function(){
	React.render(FullApp({}), document.body);
});
*/
        
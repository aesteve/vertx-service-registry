var Tags = React.createClass({
	render: function(){
		return (
			<span className="tag" click="this.removeTagFromSearch">tag</span>
		);
	},
	removeTagFromSearch: function(){
	}
});

var SearchBar = React.createClass({
	render: function(){
		return (
			<div>
				<div className="search-bar">
					<span className="search">
						<input type="text" placeholder="Search service" change="this.updateMatchingServices" />
					</span>
					<span className="filters">Filters: <Tags /></span>
					<span className="counters">{this.state.matchingServices.length} / {this.state.services.length} services</span>
					<div className="reset"></div>
				</div>
			</div>
		);
	},
	updateMatchingServices: function(){
		console.log("TODO");
	}
});


var Services = React.createClass({
	render: function(){
		return (<div className="services">services</div>);
	}
});

var Header = React.createClass({
	render: function(){
		return (<h1>Services registry</h1>);
	}
});


var FullApp = React.createClass({
	render: function(){
		return (
			<div>
				<Header />
				<SearchBar />
				<Services />
			</div>
		);
	}
});

$(document).ready(function(){
	React.render(
		<Header />, document.body);
});
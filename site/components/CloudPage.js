var React = require('react');
var d3 = require('d3');
var _ = require('underscore');
var cloud = require('./libs/d3.layout.cloud')();

var CloudTagsCollection = require('../model/PaginatedCollection');

var cloudTagsCollection = new CloudTagsCollection("2", "cloud");


var width=1000;
var height=800;

var CloudPage = React.createClass({
	componentDidUpdate:function(){
		if (!this.state.tags)
			return;
		
		var fill = d3.scale.category20();
		
		cloud.size([500, 500])
		.words(this.state.tags)
		.rotate(function() { return 0; })
		.font("Impact")
		.fontSize(function(d) { return d.size; })
		.on("end", draw)
		.start();

		function draw(words) {
			d3.select(document.getElementById('cloud')).append("svg")
			.attr("width", width)
			.attr("height", height)
			.style("display", "block")
			.style("margin", "0 auto")
			.append("g")
			.attr("transform", "translate("+width/2+","+height/2+")")
			.selectAll("text")
			.data(words)
			.enter().append("text")
			.style("font-size", function(d) { return d.size + "px"; })
			.style("cursor","pointer")
			.on("click", function(d, i){
				window.open("/services?q="+d.text);
			})
			.style("font-family", "Impact")
			.style("fill", function(d, i) { return fill(i); })
			.attr("text-anchor", "middle")
			.attr("transform", function(d) {
				return "translate(" + [d.x, d.y] + ")";
			})
			.text(function(d) { return d.text; });
		}
	},
	componentDidMount: function(){
		var instance_ = this;
		cloudTagsCollection.fetch().done(function(){
			var items = cloudTagsCollection.currentPageResources;
			var max = _.max(items, function(item){
				return item.weight;
			}).weight;
			var min = _.min(items, function(item){
				return item.weight;
			}).weight;
			var tags = _.map(items, function(tag){
				return {text:tag.word, size:instance_.getSizeForWeight(tag.weight, min, max)};
			});
			instance_.setState({tags:tags});
		});
	},
	render:function(){
		return (
				<div id="cloud">

				</div>
		);
	},
	getSizeForWeight: function(weight, min, max){
		var requiredMin = 25; // min should == 12
		var requireMax = 80; // max should == 20
		return requiredMin + (weight-min)/(max-min) * (requireMax-requiredMin);
	}
});

module.exports = CloudPage;
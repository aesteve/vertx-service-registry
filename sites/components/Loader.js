var React = require('react');


var Loader = React.createClass({
    render:function(){
        return (
            <div className='loading inline-block'>
              <div className='bullet'></div>
              <div className='bullet'></div>
              <div className='bullet'></div>
              <div className='bullet'></div>
            </div>
        );
    }
});

module.exports = Loader;
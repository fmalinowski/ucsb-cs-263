var InitialRouteForm = React.createClass({
	render: function() {
		return (
			<div className="places-form u-center u-margin-bottom-xl">
				<h2>1. Find the initial route</h2>
				<form>
					<input type="text" placeholder="Origin place" className="places-form__textbox" />
					<input type="text" placeholder="Destination place" className="places-form__textbox" />
					<input type="submit" value="Find Initial Route" className="places-form__btn" />
				</form>
			</div>
		);
	}
});

var Map = React.createClass({
	componentDidMount: function() {
		var gmap = new google.maps.Map(document.getElementById('map'), {
          center: {lat: 34.4139629, lng: -119.8511357},
          zoom: 8
        });
	},

	render: function() {
		return (
			<div id="map" className="map-container u-margin-bottom-xl"></div>
		);
	}
});

var WaypointsForm = React.createClass({
	render: function() {
		return (
			<div className="waypoints-form u-margin-bottom-xl">
				<h2 className="u-center">2. Insert the waypoint categories</h2>
				TODO: This has to be done after backend story is done
			</div>
		);
	}
});

var App = React.createClass({
	render: function() {
		return(
			<div className="App">
				<InitialRouteForm />
				<Map />
				<WaypointsForm />

				Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
				Cras ac facilisis ligula, a laoreet velit. Suspendisse aliquet ac lacus vel maximus. 
				Aenean vitae libero finibus, porta ante pellentesque, dignissim leo. Donec facilisis sagittis arcu tristique tempus. 
				Cras hendrerit augue euismod sem efficitur, egestas consectetur eros ultricies. 
				Donec vitae arcu nec velit convallis vehicula. Maecenas fringilla vulputate semper.
			</div>
		);
	}
});

ReactDOM.render(<App />, document.getElementById("container"));

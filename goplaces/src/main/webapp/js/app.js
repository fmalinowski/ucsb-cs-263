var InitialRouteForm = React.createClass({
	handleOriginChange: function(e) {
		this.setState({origin: e.target.value});
	},

	handleDestinationChange: function(e) {
		this.setState({destination: e.target.value});
	},

	handleSubmit: function(e) {
		e.preventDefault();
		var origin = this.state.origin;
		var destination = this.state.destination;
		var url = this.props.url;

		if (!origin || !destination) {
			return;
		}

		var jsonToSend = {
			origin: {
				address: origin
			},
			destination: {
				address: destination
			}
		};
		var jsonString = JSON.stringify(jsonToSend);

		$.ajax({
			url: url,
			contentType: 'application/json',
			dataType: 'json',
			type: 'POST',
			data: jsonString,
			success: function(data) {
				// Handle Data coming from server
				// We receive data.status, data.routeId, data.googledirections
				data.request = jsonToSend;
				this.props.onFormSubmit(data);
			}.bind(this),
			error: function(xhr, status, err) {
				console.error(this.props.url, status, err.toString());
			}.bind(this)
		});
	},

	render: function() {
		return (
			<div className="places-form u-center u-margin-bottom-xl">
				<h2>1. Find the initial route</h2>
				<form onSubmit={this.handleSubmit}>
					<input type="text" placeholder="Origin place" className="places-form__textbox" onChange={this.handleOriginChange} />
					<input type="text" placeholder="Destination place" className="places-form__textbox" onChange={this.handleDestinationChange} />
					<input type="submit" value="Find Initial Route" className="places-form__btn" />
				</form>
			</div>
		);
	}
});

var Map = React.createClass({
	componentDidMount: function() {
		this.gmap = new google.maps.Map(document.getElementById('map'), {
          center: {lat: 34.4139629, lng: -119.8511357},
          zoom: 8
        });
	},

	componentDidUpdate: function(prevProps, prevState) {
		if (this.props.directions) {
			this.drawInitialDirections(this.props.directions);
		}
	},

	drawInitialDirections: function(mapDirections) {
		if (!mapDirections) {
			return;
		}
		// WE CANNOT USE THE MAP DIRECTIONS FROM THE WEB SERVICE API BECAUSE FORMAT IS NOT THE SAME....
		// SO WE BUILD THE ROUTE USING THE GOOGLE JAVASCRIPT API, IT'S SUPER DUMB!!!!!

		// TO BE RECODED WITH PARSING INFO FROM SERVER AND USING CLASS FROM JAVASCRIPT API IF WE HAVE TIME LATER

		var directionsService = new google.maps.DirectionsService();
		var directionsDisplay = new google.maps.DirectionsRenderer();
		directionsDisplay.setMap(this.gmap);

		var originPlace = {
			placeId: mapDirections.geocoded_waypoints[0].place_id
		}
		var destinationPlace = {
			placeId: mapDirections.geocoded_waypoints[1].place_id
		}

		var request = {
    		origin: originPlace,
    		destination: destinationPlace,
    		travelMode: google.maps.TravelMode.DRIVING
  		};

  		directionsService.route(request, function(result, status) {
    		if (status == google.maps.DirectionsStatus.OK) {
      			directionsDisplay.setDirections(result);
    		}
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
	getInitialState: function() {
		return {
			routeID: null,
			mapDirections: null,
			request: null
		}
	},

	handleInitialRouteSubmit: function(initialRouteData) {
		// We receive initialRouteData.status, initialRouteData.routeId, initialRouteData.googledirections
		var routeID = initialRouteData.routeID;
		var googleMapDirections = initialRouteData.googledirections;
		var request = initialRouteData.request;

		var route = {
			routeID: routeID,
			mapDirections: googleMapDirections,
			request: request
		};

		console.log("APP, handleInitialRouteSubmit, routeID: " + routeID);
		console.log("APP, handleInitialRouteSubmit, googleMapDirections: " + googleMapDirections);
		
		this.setState(route);
	},

	render: function() {
		return(
			<div className="App">
				<InitialRouteForm url="/rest/routes" onFormSubmit={this.handleInitialRouteSubmit} />
				<Map directions={this.state.mapDirections} request={this.state.request} />
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

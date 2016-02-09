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
		// This code add more stuff to the JSON returned by our API because google map directions needs an array as the path field of a step
		// so we construct it. Also we need to change the names of some attributes to make it compatible with Google Map Javascript API.

		if (!mapDirections) {
			return;
		}

		if (this.directionsDisplay != null) {
			this.directionsDisplay.setMap(null);
		}

		this.directionsDisplay = new google.maps.DirectionsRenderer();

		mapDirections.routes.map(function(route) {
			route.bounds.south = route.bounds.southwest.lat;
			route.bounds.west = route.bounds.southwest.lng;
			route.bounds.north = route.bounds.northeast.lat;
			route.bounds.east = route.bounds.northeast.lng;

			delete route.bounds.southwest;
			delete route.bounds.northeast;
		});

		mapDirections.request = {
			origin: {
				pladeId: mapDirections.geocoded_waypoints[0].place_id
			},
			destination: {
				pladeId: mapDirections.geocoded_waypoints[1].place_id
			},
			travelMode: "DRIVING"
		};

		mapDirections.routes.map(function(route) {
			route.legs.map(function(leg) {
				leg.steps.map(function(step) {
					var pathArray = google.maps.geometry.encoding.decodePath(step.polyline.points);
					step.path = pathArray;
				});
			});
		});

		this.directionsDisplay.setDirections(mapDirections);
		this.directionsDisplay.setMap(this.gmap);

		// Make request with Javascript API (NO LONGER NEEDED AS WE FOUND A WORK AROUND AND USE JSON GIVEN BY OUR API)

		// var directionsService = new google.maps.DirectionsService();
		// var directionsDisplay = new google.maps.DirectionsRenderer();
		// directionsDisplay.setMap(this.gmap);

		// var originPlace = {
		// 	placeId: mapDirections.geocoded_waypoints[0].place_id
		// }
		// var destinationPlace = {
		// 	placeId: mapDirections.geocoded_waypoints[1].place_id
		// }

		// var request = {
  //   		origin: originPlace,
  //   		destination: destinationPlace,
  //   		travelMode: google.maps.TravelMode.DRIVING
  // 		};

  // 		directionsService.route(request, function(result, status) {
  //   		if (status == google.maps.DirectionsStatus.OK) {
  //     			directionsDisplay.setDirections(result);
  //   		}
  // 		});
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

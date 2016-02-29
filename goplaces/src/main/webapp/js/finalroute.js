var DrivingInstructions = React.createClass({
	render: function() {
		if (this.props.directions) {
			return (
				<div>
					<h1>Final route #{this.props.routeID}</h1>
				</div>
			)
		}
		return (
			<div>
				<h1>No route available for {this.props.routeID}</h1>
			</div>
		)
	}
});

var Map = React.createClass({
	componentDidMount: function() {
		this.gmap = new google.maps.Map(document.getElementById('map'), {
          center: {lat: 34.4139629, lng: -119.8511357},
          zoom: 8
        });

        if (this.props.directions) {
			this.drawInitialDirections(this.props.directions);
		}
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

		var geocodedWaypointsTotal = mapDirections.geocoded_waypoints.length;

		mapDirections.request = {
			origin: {
				pladeId: mapDirections.geocoded_waypoints[0].place_id
			},
			destination: {
				pladeId: mapDirections.geocoded_waypoints[geocodedWaypointsTotal-1].place_id
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
		if (this.props.directions) {
			return (
				<div id="map" className='map-container u-margin-bottom-xl'></div>
			);
		}
		return null;
	}
});

var FinalRouteApp = React.createClass({
	getInitialState: function() {
		return {
			routeID: null,
			finalRouteJSON: null
		}
	},

	retrieveFinalRoute: function(routeID) {
		console.log("retrieveFinalRoute called");

		var jsonToSend = {
			"routeID": routeID,
		};

		$.ajax({
			url: this.props.url,
			contentType: 'application/json',
			type: 'GET',
			data: jsonToSend,
			success: function(data) {
				if (data.status && data.status !== "ERROR") {
					this.setState({
						finalRouteJSON: data
					});
				}
			}.bind(this),
			error: function(xhr, status, err) {
				console.error(this.props.url, status, err.toString());
			}.bind(this)
		});
	},

	render: function() {
		var routeID = window.location.hash;
		routeID = routeID.substr(1);

		if (this.state.finalRouteJSON) {
			return(
				<div className="App">
					<Map directions={this.state.finalRouteJSON} />
					<DrivingInstructions directions={this.state.finalRouteJSON} routeID={routeID} />
				</div>
			);
		} else {
			this.retrieveFinalRoute(routeID);
		}
		return null;
	}
});

ReactDOM.render(<FinalRouteApp url="rest/get_custom_route" />, document.getElementById("container"));
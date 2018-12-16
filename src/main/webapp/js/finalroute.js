var StepInstruction = React.createClass({
	render: function() {
		var step = this.props.json;

		var htmlInstructions = {
			__html: step.html_instructions
		};

		return (
			<div className="step-instruction u-margin-bottom-xsmall">
				<span className="step-instruction__info u-margin-right-xl"><span className="icon-clock2 u-margin-right-small"></span>{step.duration.text}</span>
				<span className="step-instruction__info u-margin-right-xl"><span className="icon-make-group u-margin-right-small"></span>{step.distance.text}</span>
				<div dangerouslySetInnerHTML={htmlInstructions} />
			</div>
		);
	}
});

var LegInstructions = React.createClass({
	render: function() {
		var startAddress = this.props.json.start_address;
		var endAddress = this.props.json.end_address;
		var stepsArray = this.props.json.steps;

		var stepsInstructions = stepsArray.map(function(step, index) {
			return (
				<StepInstruction json={step} key={index} />
			);
		});

		return (
			<div className="leg-instructions">
				<h2 className="u-center">Leg {this.props.legNumber}</h2>
				<div className="u-margin-bottom-small">
					<span style={{color: 'blue'}}>From: <em>{startAddress}</em></span> <br />
					<span style={{color: 'red'}}>To: <em>{endAddress}</em></span>
				</div>
				{stepsInstructions}
			</div>
		);
	}
});

var DrivingInstructions = React.createClass({
	render: function() {
		if (this.props.directions) {
			if (!this.props.directions.routes || this.props.directions.routes.length == 0) {
				return null;
			}

			var legsJSONArray = this.props.directions.routes[0].legs;

			var legsInstructions = legsJSONArray.map(function(leg, index) {
				return (
					<LegInstructions json={leg} key={index} legNumber={index+1} />
				);
			});


			return (
				<div>
					<h1 className="u-center">Final route #{this.props.routeID}</h1>
					<div className="u-center u-margin-bottom-small">Want to share this route with a friend? Here's the link: {window.location.href}</div>
					{legsInstructions}
				</div>
			);
		}
		return (
			<div>
				<h1>No route available for {this.props.routeID}</h1>
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
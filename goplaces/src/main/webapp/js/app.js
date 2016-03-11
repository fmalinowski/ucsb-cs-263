/*
 React component that handles the initial route form (origin, destination and submit button)
 */
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

		console.log("InitialRouteForm - Submitting initial route");

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
				console.log("InitialRouteForm - Got initial route from backend");
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
					<input type="text" placeholder="Origin place" className="places-form__textbox js-places-form-origin" onChange={this.handleOriginChange} />
					<input type="text" placeholder="Destination place" className="places-form__textbox js-places-form-destination" onChange={this.handleDestinationChange} />
					<input type="submit" value="Find Initial Route" className="form-submit-btn js-places-form-submit" />
				</form>
			</div>
		);
	}
});

/*
 Google map React component
 */
var Map = React.createClass({
	componentDidMount: function() {
		this.gmap = new google.maps.Map(document.getElementById('map'), {
          center: {lat: 34.4139629, lng: -119.8511357},
          zoom: 8
        });
        this.placesSelected = {};
        console.log("Map - google map is mounted");
	},

	componentDidUpdate: function(prevProps, prevState) {
		if (this.props.directions) {
			console.log("Map - is updated");
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

		console.log("Map - drawing route on map");

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
	},

	drawMarkerForPlace: function(placeJSON, color) {
		var pinColor = color.substr(1);

    	var pinImage = new google.maps.MarkerImage("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + pinColor,
        new google.maps.Size(21, 34),
        new google.maps.Point(0,0),
        new google.maps.Point(10, 34));

		var marker = new google.maps.Marker({
			position: placeJSON.geometry.location,
			map: this.gmap,
			title: placeJSON.name,
			icon: pinImage,
			optimized: false
		});

		var infowindow = new google.maps.InfoWindow();

  		var map = this.gmap;

  		var selector = '.js-iw-' + placeJSON.place_id + ' .js-select-place';

  		$(document).on('click', selector, function(e) {
			this.handleSelectedWaypoint(placeJSON, marker, pinColor);
		}.bind(this));

		marker.addListener('click', function() {
			infowindow.setContent(this.getContentForInfoWindow(placeJSON));

			if (this.infoWindow) {
				this.infoWindow.close();
			}
			this.infoWindow = infowindow;
			infowindow.open(map, marker);
  		}.bind(this));
	},

	handleSelectedWaypoint: function(placeJSON, marker, defaultPinColor) {
		var pinColor;
		var $selectButton = $(document).find('.js-iw-' + placeJSON.place_id + ' .js-select-place');

		var isPlaceSelected = this.placesSelected[placeJSON.place_id];

		if (isPlaceSelected === true) {
			this.placesSelected[placeJSON.place_id] = false;

    		pinColor = defaultPinColor;
    		$selectButton.text('Select Waypoint');
		} else {
			pinColor = '000000';
			$selectButton.text('Unselect Waypoint');
			this.placesSelected[placeJSON.place_id] = true;
		}

		var pinImage = new google.maps.MarkerImage("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + pinColor,
        	new google.maps.Size(21, 34),
        	new google.maps.Point(0,0),
        	new google.maps.Point(10, 34));

		marker.setIcon(pinImage);

		this.props.handleSelectedWaypoints(this.placesSelected);
	},

	getContentForInfoWindow: function(placeJSON) {
		var textForWaypoint = this.placesSelected[placeJSON.place_id] ? "Unselect Waypoint" : "Select Waypoint";
		var buttonCode = '<button class="js-select-place" data-placeId="' + placeJSON.place_id + '">' + textForWaypoint + '</button>';

		var placeAddress = placeJSON.vicinity != null ? placeJSON.vicinity : 'n/a';

		var contentString = '<div id="content" class="js-iw-' + placeJSON.place_id + '">' +
		'<h2>' + placeJSON.name + '</h2>' +
		'<strong>Address</strong>: ' + placeAddress +
		'<br>' + buttonCode +
		'</div>';

		$('.js-select-place')

		return contentString;
	},

	displayPlacesMarkers: function(placesJSONObject) {
		console.log("displayPlacesMarkers called");
		var places = placesJSONObject.places;
		var colors = placesJSONObject.colors;

		console.log("Map - display place markers");

		for (var key in places) {
			var placesForKey = places[key];

			for (var i = 0; i < placesForKey.length; i++) {
				this.drawMarkerForPlace(placesForKey[i], colors[key]);
			}
		}
	},

	render: function() {
		var cssClasses = 'map-container u-margin-bottom-xl';

		if (this.props.places) {
			this.displayPlacesMarkers(this.props.places);
			var cssClasses = 'map-container u-margin-bottom-small';
		}

		return (
			<div id="map" className={cssClasses}></div>
		);
	}
});

/*
 React component that handles a single item of the legend below the google map
 */
var MapLegendItem = React.createClass({
	render: function() {
		return (
			<li className="map-legend-item"><span className="map-legend-item__color" style={{background: this.props.color}}></span>{this.props.name}</li>
		);
	}
});

/*
 React component that handles the legend of the map (below the map)
 */
var MapLegend = React.createClass({
	render: function() {
		if (this.props.colorLegend) {
			console.log("MapLegend - colorLegend is ready");
			var mapLegendItems = [];
			var colors = this.props.colorLegend;

			for (var key in colors) {
				mapLegendItems.push(<MapLegendItem color={colors[key]} name={key} key={key} />);
			}

			return (
				<div className="u-margin-bottom-xl">
					<ul className="map-legend">
						{mapLegendItems}
					</ul>
				</div>
			);
		}
		return null;
	}
});

/*
 Waypoint input text field React component (to add a waypoint category)
 */
var WaypointEntry = React.createClass({
	getInitialState: function() {
		return {
			value: this.props.value
		}
	},

	handleValueChange: function(e) {
		this.setState({value: e.target.value});
		this.props.onValueChange(e.target.value, this.props.categoryIndex);
	},

	render: function() {
		return (
			<input type="text" className="js-waypoint-form-textfield" placeholder="Insert a waypoint category here" style={{width: "100%"}} value={this.state.value} onChange={this.handleValueChange} />
		);
	}
});

/*
 React component that handles the form to submit the waypoint categories
 */
var WaypointsForm = React.createClass({
	getInitialState: function() {
		return {
			waypointCategories: ['']
		};
	},

	handleCategoryValueChange: function(newValue, index) {
		var waypointCategories = this.state.waypointCategories;
		waypointCategories[index] = newValue;

		this.setState({waypointCategories: waypointCategories});
	},

	handleAddCategory: function(e) {
		e.preventDefault();

		var waypointCategories = this.state.waypointCategories;
		waypointCategories.push('');
		console.log("WaypointsForm - handleAddCategory");

		this.setState({waypointCategories: waypointCategories});	
	},

	handleSubmitCategories: function(e) {
		e.preventDefault();

		// var categoriesArrayString = JSON.stringify(this.state.waypointCategories);
		var url = this.props.url;
		var jsonToSend = {
			routeID: this.props.routeID,
			keywords: this.state.waypointCategories,
			radius: this.props.radius
		};
		var jsonString = JSON.stringify(jsonToSend);

		// TODO: Send here the ajax to server with the waypoints we want to submit
		console.log("WaypointsForm, handleSubmitCategories, submit: " + jsonString);
		$(".loading-screen").removeClass("loading-screen--hidden");

		$.ajax({
			url: url,
			contentType: 'application/json',
			dataType: 'json',
			type: 'POST',
			data: jsonString,
			success: function(data) {
				setTimeout(this.pollWaypoints, 1000);
				console.log("WaypointsForm, handleSubmitCategories, success: " + JSON.stringify(data));
			}.bind(this),
			error: function(xhr, status, err) {
				$(".loading-screen").addClass("loading-screen--hidden");
				console.error(this.props.url, status, err.toString());
			}.bind(this)
		});
		
	},

	pollWaypoints: function() {
		var url = this.props.url;
		var jsonToSend = {
			"routeID": this.props.routeID.toString(),
		};

		$.ajax({
			url: url,
			contentType: 'application/json',
			type: 'GET',
			data: jsonToSend,
			success: function(data) {
				if (data.status === "OK") {
					// We got here the places to display on the map
					console.log("WaypointsForm, pollWaypoints, got places:");
					console.log(JSON.parse(data.places));
					this.props.onPolledPlaces(JSON.parse(data.places));
					$(".loading-screen").addClass("loading-screen--hidden");
				} else if (data.status === "POLL") {
					console.log("WaypointsForm - Polling data");
					setTimeout(this.pollWaypoints, 1000);
				}

				console.log("WaypointsForm, pollWaypoints, success, status: " + data.status);
			}.bind(this),
			error: function(xhr, status, err) {
				console.error(this.props.url, status, err.toString());
			}.bind(this)
		});
	},

	render: function() {
		var that = this;

		var waypointNodes = this.state.waypointCategories.map(function(category, index) {
			return (
				<WaypointEntry key={index} categoryIndex={index} value={category} onValueChange={that.handleCategoryValueChange} />
			);
		});

		return (
			<div className="waypoints-form u-center u-margin-bottom-xl">
				<h2 className="u-center">2. Insert the waypoint categories</h2>
				
				<div className="u-margin-bottom-small">Insert the categories of waypoints you want to have on your route. <br />
				e.g.: Café, night club, museum, restaurant, zoo, etc.</div>
				
				<form onSubmit={this.handleSubmitCategories}>
					{waypointNodes}

					<div className="waypoints-form__submit-add">
						<a href="#" className="form-add-btn u-float-right" onClick={this.handleAddCategory}>+</a>
						<input type="submit" value="Submit Waypoint Categories" className="form-submit-btn js-waypoints-form-submit-btn" />
					</div>
				</form>
			</div>
		);
	}
});

/*
 React component that handles the submission of the finalized route after having selected all the waypoints on the map
 */
var FinalRouteController = React.createClass({
	handleWaypointsSubmit: function() {
		var jsonToSend = {
			waypoints: this.props.selectedPlaces,
			routeID: this.props.routeID
		};

		var jsonString = JSON.stringify(jsonToSend);
		console.log("FinalRouteController - submitting selected waypoints on map");

		$.ajax({
			url: this.props.url,
			contentType: 'application/json',
			dataType: 'json',
			type: 'POST',
			data: jsonString,
			success: function(data) {
				console.log("handleWaypointsSubmit, success: " + data);
				window.location.href = '/finalroute.html#' + this.props.routeID;
			}.bind(this),
			error: function(xhr, status, err) {
				console.error(this.props.url, status, err.toString());
			}.bind(this)
		});
	},

	render: function() {
		if (this.props.selectedPlaces) {
			return (
				<div className="final-route-bar">
					<a href="#" className="form-submit-btn form-submit-btn--red" onClick={this.handleWaypointsSubmit}>Finalize Route</a>
				</div>
			)
		}
		return null;
	}
});

/*
 Main React Component (handles the whole page)
 */
var App = React.createClass({
	getInitialState: function() {
		return {
			routeID: null,
			mapDirections: null,
			request: null,
			places: null,
			selectedPlaces: null,
			colorLegend: null
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

	generateColorsForPlaces: function(placesJSONObject) {
		var placesColors = {};
		var numberOfPlaces = Object.keys(placesJSONObject).length;
		var colors = randomColor({count: numberOfPlaces});
		var currentPos = 0;

		for (var key in placesJSONObject) {
			placesColors[key] = colors[currentPos];
			currentPos++;
		}
		return placesColors;
	},

	handleReturnedPlaces: function(placesJSONObject) {
		var placesColors = this.generateColorsForPlaces(placesJSONObject);
		var placesObject = {
			places: placesJSONObject,
			colors: placesColors
		}

		var placesForState = {
			mapDirections: null,
			places: placesObject,
			colorLegend: placesColors
		}
		this.setState(placesForState);
	},

	handleSelectedWaypoints: function(selectedPlacesJSON) {
		console.log("handleSelectedWaypoints called");

		var selectedPlacesArray = [];
		for (var place in selectedPlacesJSON) {
			if (selectedPlacesJSON[place]) {
				selectedPlacesArray.push(place);
			}
		}

		this.setState({
			selectedPlaces: selectedPlacesArray,
			places: null
		});
	},

	render: function() {
		return(
			<div className="App">
				<FinalRouteController selectedPlaces={this.state.selectedPlaces} routeID={this.state.routeID} url="/rest/get_custom_route" />
				<InitialRouteForm url="/rest/routes" onFormSubmit={this.handleInitialRouteSubmit} />
				<Map directions={this.state.mapDirections} request={this.state.request} places={this.state.places} handleSelectedWaypoints={this.handleSelectedWaypoints}/>
				<MapLegend colorLegend={this.state.colorLegend} />
				<WaypointsForm url="/rest/select_waypoints" routeID={this.state.routeID} radius={5000} onPolledPlaces={this.handleReturnedPlaces} />
			</div>
		);
	}
});

ReactDOM.render(<App />, document.getElementById("container"));

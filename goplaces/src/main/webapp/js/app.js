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

		$(".loading-screen").removeClass("loading-screen--hidden");

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
				$(".loading-screen").addClass("loading-screen--hidden");
			}.bind(this),
			error: function(xhr, status, err) {
				console.error(this.props.url, status, err.toString());
				$(".loading-screen").addClass("loading-screen--hidden");
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

var ReviewItem = React.createClass({
	render: function() {
		var review = this.props.review;

		return (
			<div className="review-item">
				<strong>Author: </strong> {review.author_name}<br />
				<strong>Rating: </strong> {review.rating}<br />
				<div className="review-item__comment">{review.text}</div>
			</div>
		);
	}
});

/*
	React component displaying the reviews of a place
*/
var ReviewsDisplayer = React.createClass({
	render: function() {
		if (!this.props.reviews) {
			return null;
		}

		var reviewsObj = this.props.reviews;
		var rating = reviewsObj.rating ? (reviewsObj.rating + '/5') : 'unknown';

		var reviewItems = reviewsObj.reviews.map(function(review, index) {
			return (
				<ReviewItem review={review} key={index} />
			);
		});

		return (
			<div className="reviews-displayer">
				<h3 className="u-center u-margin-bottom-xsmall">Reviews</h3>
				<h4 className="u-center u-margin-top-xsmall">Rated: {rating}</h4>
				{reviewItems}
			</div>
		);
	}
});

/*
 Google map React component
 */
var Map = React.createClass({
	getInitialState: function() {
		this.markers = [];
		this.places = null;

		return {
			reviews: null
		};
	},

	componentDidMount: function() {
		this.gmap = new google.maps.Map(document.getElementById('map'), {
          center: {lat: 34.4139629, lng: -119.8511357},
          zoom: 8
        });
        this.placesSelected = {};
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

			// Fetch the reviews
			this.fetchReviewsForPlace(placeJSON.place_id, placeJSON.name);

  		}.bind(this));

		infowindow.addListener('closeclick', function() {
			// Close the reviews
			this.setState({reviews: null});
		}.bind(this));

		this.markers.push(marker);
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

		return contentString;
	},

	displayPlacesMarkers: function(placesJSONObject) {
		this.clearMap();
		this.places = placesJSONObject.places;

		var places = placesJSONObject.places;
		var colors = placesJSONObject.colors;

		for (var key in places) {
			var placesForKey = places[key];

			for (var i = 0; i < placesForKey.length; i++) {
				this.drawMarkerForPlace(placesForKey[i], colors[key]);
			}
		}
	},

	fetchReviewsForPlace: function(placeId, placeName) {
		var reviewsUrl = this.props.reviewsUrl + "/" + placeId;

		this.setState({reviews: null});

		$.ajax({
			url: reviewsUrl,
			contentType: 'application/json',
			type: 'GET',
			success: function(data) {

				if (data.status !== null && Array.isArray(data.status) && data.status.length > 0 && data.status[0] === 'OK') {
					var rating = (data.rating !== null && Array.isArray(data.rating) && data.rating.length > 0) ? data.rating[0] : null;
					var reviewsArray = (data.reviews !== null && Array.isArray(data.reviews) && data.reviews.length > 0) ? JSON.parse(data.reviews[0]) : null;
					
					var reviews = {
						placeName: placeName,
						rating: rating,
						reviews: reviewsArray
					}

					this.setState({reviews: reviews});
				}
			}.bind(this),
			error: function(xhr, status, err) {
				console.error(reviewsUrl, status, err.toString());
			}.bind(this)
		});
	},

	clearMap: function() {
		for (var i = 0; i < this.markers.length; i++) {
			this.markers[i].setMap(null);
		}
		this.markers = [];
		this.places = null;
		this.placesSelected = {};
	},

	shouldRenderMarkers: function() {
		var newPlaces = this.props.places !== null && this.props.places.places !== null ? this.props.places.places : null;
		if (this.places == null && newPlaces !== null) {
			return true;
		}
		if (this.places !== null && newPlaces !== null) {
			var keysNumberInPlaces = Object.keys(this.places).length;
			var keysNumberInNewPlacesObject = Object.keys(newPlaces).length;
			if (keysNumberInPlaces !== keysNumberInNewPlacesObject) {
				return true;
			}
			for (var key in this.places) {
				if (newPlaces[key] === undefined) {
					return true;
				}
			}
			return false;
		}
		return false;
	},

	shouldClearMap: function() {
		if (this.routeID !== this.props.routeID) {
			this.routeID = this.props.routeID;
			return true;
		}

		return false;
	},

	render: function() {
		var cssClasses = 'map-container u-margin-bottom-xl';
		var reviews = this.state.reviews;

		if (this.shouldClearMap()) {
			this.clearMap();
			reviews = null;
		}

		if (this.shouldRenderMarkers()) {
			this.displayPlacesMarkers(this.props.places);
			var cssClasses = 'map-container u-margin-bottom-small';
		}

		return (
			<div style={{position: "relative"}}>
				<div id="map" className={cssClasses}></div>
				<ReviewsDisplayer reviews={reviews} />
			</div>
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
					Places are fetched in a radius of 10km along the route
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

		this.setState({waypointCategories: waypointCategories});	
	},

	handleSubmitCategories: function(e) {
		e.preventDefault();

		var url = this.props.url;
		var jsonToSend = {
			routeID: this.props.routeID,
			keywords: this.state.waypointCategories,
			radius: this.props.radius
		};
		var jsonString = JSON.stringify(jsonToSend);

		$(".loading-screen").removeClass("loading-screen--hidden");

		this.pollAttempts = 0;

		$.ajax({
			url: url,
			contentType: 'application/json',
			dataType: 'json',
			type: 'POST',
			data: jsonString,
			success: function(data) {
				setTimeout(this.pollWaypoints, 1000);
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
					this.props.onPolledPlaces(JSON.parse(data.places));
					$(".loading-screen").addClass("loading-screen--hidden");
				} else if (data.status === "POLL") {
					this.pollAttempts++;

					if (this.pollAttempts > 50) {
						// We polled for more than 70s. We should stop the query
						alert("Congratulations, you broke our system! This query is taking too long... We don't support long routes or too many keywords yet.");
						$(".loading-screen").addClass("loading-screen--hidden");
						this.props.onPolledPlaces(null);
					} else {
						setTimeout(this.pollWaypoints, 1000);
					}
				}

			}.bind(this),
			error: function(xhr, status, err) {
				console.error(this.props.url, status, err.toString());
			}.bind(this)
		});
	},

	render: function() {
		var that = this;

		if (this.props.routeID === null || this.props.arePlacesPolled) {
			return null;
		}

		var waypointNodes = this.state.waypointCategories.map(function(category, index) {
			return (
				<WaypointEntry key={index} categoryIndex={index} value={category} onValueChange={that.handleCategoryValueChange} />
			);
		});

		return (
			<div className="waypoints-form u-center u-margin-bottom-xl">
				<h2 className="u-center">2. Insert the waypoint categories</h2>
				
				<div className="u-margin-bottom-small">Insert the categories of waypoints you want to have on your route. <br />
				e.g.: Art museum, pet park, night club, zoo, chinese restaurant etc.</div>
				
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

		$.ajax({
			url: this.props.url,
			contentType: 'application/json',
			dataType: 'json',
			type: 'POST',
			data: jsonString,
			success: function(data) {
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
			colorLegend: null,
			arePlacesPolled: false
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
			request: request, 
			places: null,
			selectedPlaces: null,
			colorLegend: null,
			arePlacesPolled: false
		};
		
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
		if (placesJSONObject === null) {
			return;
		}
		var placesColors = this.generateColorsForPlaces(placesJSONObject);
		var placesObject = {
			places: placesJSONObject,
			colors: placesColors
		}

		var placesForState = {
			mapDirections: null,
			places: placesObject,
			colorLegend: placesColors,
			selectedPlaces: null,
			arePlacesPolled: true
		}
		this.setState(placesForState);
	},

	handleSelectedWaypoints: function(selectedPlacesJSON) {
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
				<Map routeID={this.state.routeID} directions={this.state.mapDirections} request={this.state.request} places={this.state.places} handleSelectedWaypoints={this.handleSelectedWaypoints} reviewsUrl="/rest/waypointsreviewapi" />
				<MapLegend colorLegend={this.state.colorLegend} />
				<WaypointsForm url="/rest/select_waypoints" routeID={this.state.routeID} radius={10000} onPolledPlaces={this.handleReturnedPlaces} arePlacesPolled={this.state.arePlacesPolled} />
			</div>
		);
	}
});

ReactDOM.render(<App />, document.getElementById("container"));

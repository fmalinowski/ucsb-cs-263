# Test using cURL commands

# Places Datastore API
# post a place to datastore
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data "{\"name\":\"UCSB\",\"address\":\"At the Pacific shore,California\",\"latitude\":5.6,\"longitude\":6.6,\"rating\":5.0,\"reviews\":\"its magnificient\",\"googlePlaceId\":\"example\"}" http://go-places-ucsb.appspot.com/rest/places
# get the same _place_ from datastore
curl "http://go-places-ucsb.appspot.com/rest/places/example"
echo

# Places Memcache API
# post a place to memcache
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data "{\"name\":\"UCSB\",\"address\":\"At the Pacific shore,California\",\"latitude\":5.6,\"longitude\":6.6,\"rating\":5.0,\"reviews\":\"its magnificient\",\"googlePlaceId\":\"example\"}" http://go-places-ucsb.appspot.com/rest/placesmemcache
# get the same place from memcache
curl "http://go-places-ucsb.appspot.com/rest/places/example"
echo

# Routes Datastore API
# post a route to datatsore
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data "{\"origin\":{\"address\":\"santa barbara\"},\"destination\":{\"address\":\"los angeles\"},\"mapJsonAsText\":\"xyz\"}" http://go-places-ucsb.appspot.com/rest/routesapi
# get the same route from datastore
# route_id=set_this_to_a_known_id_in_datastore_or_the_key_returned_on_running_the preceding_curl_command
# curl "http://go-places-ucsb.appspot.com/rest/routesapi/$route_id"
echo

# Routes Memcache API
# post a route to memcache
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data "{\"origin\":{\"address\":\"santa barbara\"},\"destination\":{\"address\":\"los angeles\"},\"mapJsonAsText\":\"xyz\"}" http://go-places-ucsb.appspot.com/rest/routesmemcache
# get the same route from datastore
# route_id=set_this_to_a_known_id_in_datastore_or_the_key_returned_on_running_the preceding_curl_command
# curl "http://go-places-ucsb.appspot.com/rest/routesmemcache/$route_id"
echo

# Waypoints Review API
# post a Google Place ID to fetch reviews for and store them in the datastore
curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{"place_ids":"[ChIJ1YMtb8cU6YARSHa612Q60cg]"}' "http://go-places-ucsb.appspot.com/rest/waypointsreviewapi"
sleep 2
# get reviews for the same place
curl -X GET -H "Content-Type: application/json" -H "Cache-Control: no-cache" "http://go-places-ucsb.appspot.com/rest/waypointsreviewapi/ChIJ1YMtb8cU6YARSHa612Q60cg"
echo

#BoxRouteWorker API
# post a query to fire off a task to find potential waypoints on a route
# route_id=insert_known_route_id_here
# curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -H "Postman-Token: e21b5c97-cddf-1ae6-c2a9-96d7c3eb08de" -d '{"routeID":"$route_id","radius":"10","keywords":["pet park","museum"]}' "http://go-places-ucsb.appspot.com/rest/boxrouteworkerapi"

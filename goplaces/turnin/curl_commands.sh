#!/usr/bin/env bash
# Test using cURL commands

# Route ID - 5073076857339904. This route exists in the datastore and can be used for testing.

echo
echo "Places Datastore API test"
echo

# Places Datastore API
echo "post a place to datastore"
echo
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data "{\"name\":\"UCSB\",\"address\":\"At the Pacific shore,California\",\"latitude\":5.6,\"longitude\":6.6,\"rating\":5.0,\"reviews\":\"its magnificient\",\"googlePlaceId\":\"example\"}" http://go-places-ucsb.appspot.com/rest/places
echo
echo "get the same _place_ from datastore"
echo
curl "http://go-places-ucsb.appspot.com/rest/places/example"

echo
echo
echo "Routes Datastore API test"
echo


# Routes Datastore API
echo "post a route to datatsore"
echo
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data "{\"origin\":{\"address\":\"santa barbara\"},\"destination\":{\"address\":\"los angeles\"},\"mapJsonAsText\":\"xyz\"}" http://go-places-ucsb.appspot.com/rest/routesapi
echo
echo "get the same route from datastore"
echo
route_id=5073076857339904
curl "http://go-places-ucsb.appspot.com/rest/routesapi/$route_id"

echo
echo
echo "Places Memcache API test"
echo


# Places Memcache API
echo "post a place to memcache"
echo
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data "{\"name\":\"UCSB\",\"address\":\"At the Pacific shore,California\",\"latitude\":5.6,\"longitude\":6.6,\"rating\":5.0,\"reviews\":\"its magnificient\",\"googlePlaceId\":\"example\"}" http://go-places-ucsb.appspot.com/rest/placesmemcache
echo
echo "get the same place from memcache"
echo
curl "http://go-places-ucsb.appspot.com/rest/places/example"

echo
echo
echo "Routes Memcache API test"
echo


# Routes Memcache API
echo "post a route to memcache"
echo
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data "{\"origin\":{\"address\":\"santa barbara\"},\"destination\":{\"address\":\"los angeles\"},\"mapJsonAsText\":\"xyz\"}" http://go-places-ucsb.appspot.com/rest/routesmemcache
echo
echo "get a different route from memcache, if it exists"
echo
route_id=5073076857339904
curl "http://go-places-ucsb.appspot.com/rest/routesmemcache/route--1849951270638577460"

echo
echo
echo "Waypoints Review API test"
echo

# Waypoints Review API
echo "post a Google Place ID to fetch reviews for and store them in the datastore"
echo
curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{"place_ids":"[ChIJ1YMtb8cU6YARSHa612Q60cg]"}' "http://go-places-ucsb.appspot.com/rest/waypointsreviewapi"
sleep 2
echo
echo "get reviews for the same place"
echo
curl -X GET -H "Content-Type: application/json" -H "Cache-Control: no-cache" "http://go-places-ucsb.appspot.com/rest/waypointsreviewapi/ChIJ1YMtb8cU6YARSHa612Q60cg"

echo
echo
echo "BoxRouteWorker API test"
echo 

#BoxRouteWorker API
echo "post a query to fire off a task to find potential waypoints on a route"
echo
route_id=5073076857339904
curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -H "Postman-Token: e21b5c97-cddf-1ae6-c2a9-96d7c3eb08de" -d '{"routeID":"5073076857339904","radius":"10","keywords":["pet park","museum"]}' "http://go-places-ucsb.appspot.com/rest/boxrouteworkerapi"
curl -X GET -H "Content-Type: application/json" -H "Cache-Control: no-cache" -H "Postman-Token: 888a79a6-8bad-73cd-df51-c90edb0aba6a" "http://go-places-ucsb.appspot.com/rest/boxrouteworkerapi/5081456606969856"
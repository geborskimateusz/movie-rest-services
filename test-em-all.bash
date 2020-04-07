#!/usr/bin/env bash

# Sample usage:
#
#   HOST=localhost PORT=7000 ./test-em-all.bash
#
: ${HOST=localhost}
: ${PORT=8443}
: ${MOV_ID_REVS_RECS=2}
: ${MOV_ID_NOT_FOUND=14}
: ${MOV_ID_NO_RECS=114}
: ${MOV_ID_NO_REVS=214}

function assertCurl() {

  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && ((${#result} > 3)) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]; then
    if [ "$httpCode" = "200" ]; then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
    return 0
  else
    echo "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
    echo "- Failing command: $curlCmd"
    echo "- Response Body: $RESPONSE"
    return 1
  fi
}

function assertEqual() {

  local expected=$1
  local actual=$2
  local message=$3

  printf "Test case: $message -> "

  if [ "$actual" = "$expected" ]; then
    echo "Test OK (actual value: $actual)"
    return 0
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    return 1
  fi
}

function testUrl() {
  url=$@
  if $url -ks -f -o /dev/null; then
    return 0
  else
    return 1
  fi
}

function waitForService() {
  url=$@
  echo -n "Wait for: $url... "
  n=0
  until testUrl $url; do
    n=$((n + 1))
    if [[ $n == 100 ]]; then
      echo " Give up"
      exit 1
    else
      sleep 3
      echo -n ", retry #$n "
    fi
  done
  echo "DONE, continues..."
}

function testCompositeCreated() {

  # Expect that the Movie Composite for productId $MOV_ID_REVS_RECS has been created with three recommendations and three reviews
  if ! assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_RECS -s"; then
    echo -n "FAIL"
    return 1
  fi

  set +e
  assertEqual "$MOV_ID_REVS_RECS" $(echo $RESPONSE | jq .movieId)
  if [ "$?" -eq "1" ]; then return 1; fi

  assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
  if [ "$?" -eq "1" ]; then return 1; fi

  assertEqual 1 $(echo $RESPONSE | jq ".reviews | length")
  if [ "$?" -eq "1" ]; then return 1; fi

  set -e
}

function waitForMessageProcessing() {
  echo "Wait for messages to be processed... "

  # Give background processing some time to complete...
  sleep 1

  n=0
  until testCompositeCreated; do
    n=$((n + 1))
    if [[ $n == 40 ]]; then
      echo " Give up"
      exit 1
    else
      sleep 3
      echo -n ", retry #$n "
    fi
  done
  echo "All messages are now processed!"
}

function recreateComposite() {
  local movieId=$1
  local composite=$2

  assertCurl 200 "curl $AUTH -X DELETE -k https://$HOST:$PORT/movie-composite/${movieId} -s"
  curl -X POST -k https://$HOST:$PORT/movie-composite -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" --data "$composite"
}

function setupData() {

  body="{\"movieId\":$MOV_ID_REVS_RECS"
  body+=',"genre": "Science Fiction",
    "title": "Star Wars 1",
    "recommendations": [
      {
        "author": "John Doe",
        "content": "Fake content of John Doe",
        "rate": 5,
        "recommendationId": 1
      },
      {
        "author": "Jane Doe",
        "content": "Fake content of Jane Doe",
        "rate": 4,
        "recommendationId": 2
      },
      {
        "author": "Mike Fake",
        "content": "Fake content of Mike Fake",
        "rate": 2,
        "recommendationId": 3
      }
    ],
    "reviews": [
      {
        "reviewId": 1,
        "author": "Jane Doe",
        "content": "Fake Review content of Jane Doe",
        "subject": "Review of Jane Doe"
      }
    ]
  }'
  recreateComposite "$MOV_ID_REVS_RECS" "$body"

  body="{\"movieId\":$MOV_ID_NO_RECS"
  body+=',"genre": "Science Fiction",
      "title": "Star Wars 1",
      "reviews": [
        {
          "reviewId": 2,
          "author": "Jane Doe",
          "content": "Fake Review content of Jane Doe",
          "subject": "Review of Jane Doe"
        },
        {
          "reviewId": 3,
          "author": "Jane Doe",
          "content": "Fake Review content of Jane Doe",
          "subject": "Review of Jane Doe"
        },
        {
          "reviewId": 4,
          "author": "Jane Doe",
          "content": "Fake Review content of Jane Doe",
          "subject": "Review of Jane Doe"
        }
      ]
    }'
  recreateComposite "$MOV_ID_NO_RECS" "$body"

  body="{\"movieId\":$MOV_ID_NO_REVS"
  body+=',"genre": "Science Fiction",
      "title": "Star Wars 1",
      "recommendations": [
        {
          "author": "John Doe",
          "content": "Fake content of John Doe",
          "rate": 5,
          "recommendationId": 4
        },
        {
          "author": "Jane Doe",
          "content": "Fake content of Jane Doe",
          "rate": 4,
          "recommendationId": 5
        },
        {
          "author": "Mike Fake",
          "content": "Fake content of Mike Fake",
          "rate": 2,
          "recommendationId": 6
        }
      ]
    }'
  recreateComposite "$MOV_ID_NO_REVS" "$body"
}

function testCircuitBreaker() {

  echo "Start Circuit Breaker Test"
  EXEC="docker run --rm -it --network=my-network alpine"

  #Verify that circuit breaker is closed via health endpoint
  assertEqual "CLOSED" "$($EXEC wget movie-composite:8080/actuator/health -qO - | jq -r .components.movieCircuitBreaker.details.state)" "Verify that circuit breaker has status CLOSED"

  assertCurl 500 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_RECS?delay=3 $AUTH -s"
  message=$(echo $RESPONSE | jq -r .message)
  assertEqual "Did not observe any item or terminal signal within 2000ms" "${message:0:57}"

  #Verify that ciruit breaker is open
  for ((n = 0; n < 3; n++)); do
    assertCurl 500 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_RECS?delay=3 $AUTH -s"
    message=$(echo $RESPONSE | jq -r .message)
    assertEqual "CircuitBreaker 'movie' is open" "${message:0:57}"
  done

  echo "Will sleep for 10 sec waiting for the CB to go Half Open..."
  sleep 10

  #Verify that circuit breaker is closed via health endpoint
  assertEqual "HALF_OPEN" "$($EXEC wget movie-composite:8080/actuator/health -qO - | jq -r .components.movieCircuitBreaker.details.state)" "Verify that circuit breaker has status CLOSED"

  #Regular calls to close Circuit Breaker
  for ((i = 0; i < 3; i++)); do
    assertCurl 200 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_RECS $AUTH -s"
  done

  #Verify that circuit breaker is closed again
  assertEqual "CLOSED" "$($EXEC wget movie-composite:8080/actuator/health -qO - | jq -r .components.movieCircuitBreaker.details.state)" "Verify that circuit breaker is CLOSED again"

  # Verify that the expected state transitions happened in the circuit breaker
  assertEqual "CLOSED_TO_OPEN" "$($EXEC wget movie-composite:8080/actuator/circuitbreakerevents/movie/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-3].stateTransition)" "Verify CLOSED_TO_OPEN"
  assertEqual "OPEN_TO_HALF_OPEN" "$($EXEC wget movie-composite:8080/actuator/circuitbreakerevents/movie/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-2].stateTransition)" "Verify OPEN_TO_HALF_OPEN"
  assertEqual "HALF_OPEN_TO_CLOSED" "$($EXEC wget movie-composite:8080/actuator/circuitbreakerevents/movie/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-1].stateTransition)" "Verify HALF_OPEN_TO_CLOSED"
}

set -e

echo "Start Tests:" $(date)

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]; then

  echo "Restarting the test environment..."

  echo "$ docker-compose down --remove-orphans"
  docker-compose down --remove-orphans
  echo "$ docker-compose up -d"
  docker-compose up -d
fi

waitForService curl -k https://$HOST:$PORT/actuator/health

#Testing with the local authorization server
ACCESS_TOKEN=$(curl -k https://writer:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=user -d password=password -s | jq .access_token -r)

#Testing with an OpenID Connect provider – Auth0
#SCOPE="movie:read movie:write"
#read ACCESS_TOKEN < <(./get-access-token.bash $SCOPE)

AUTH="-H \"Authorization: Bearer $ACCESS_TOKEN\""

setupData

waitForMessageProcessing

#  Test messages
ID_CONFIRMATION="Comparing id's."
RECOMMENDATIONS_CONFIRMATION="Comparing recommendations length."
REVIEWS_CONFIRMATION="Comparing reviews length."

# Verify that a normal request works, expect three recommendations and three reviews
assertCurl 200 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_RECS $AUTH -s"
assertEqual "$MOV_ID_REVS_RECS" $(echo $RESPONSE | jq .movieId) "$ID_CONFIRMATION"
assertEqual 3 $(echo $RESPONSE | jq ".recommendations  | length") "$RECOMMENDATIONS_CONFIRMATION"
assertEqual 1 $(echo $RESPONSE | jq ".reviews | length") "$REVIEWS_CONFIRMATION"

# Verify that a 404 (Not Found) error is returned for a non existing movieId ($MOV_ID_NOT_FOUND)
assertCurl 404 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_NOT_FOUND $AUTH -s"

# Verify that no recommendations are returned for movieId $MOV_ID_NO_RECS
assertCurl 200 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_NO_RECS $AUTH -s"
assertEqual "$MOV_ID_NO_RECS" $(echo $RESPONSE | jq .movieId) "$ID_CONFIRMATION"
assertEqual 0 $(echo $RESPONSE | jq ".recommendations | length") "$RECOMMENDATIONS_CONFIRMATION"
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length") "$REVIEWS_CONFIRMATION"

# Verify that no reviews are returned for movieId $MOV_ID_NO_REVS
assertCurl 200 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_NO_REVS $AUTH -s"
assertEqual $MOV_ID_NO_REVS $(echo $RESPONSE | jq .movieId) "$ID_CONFIRMATION"
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length") "$RECOMMENDATIONS_CONFIRMATION"
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length") "$REVIEWS_CONFIRMATION"

## Verify that a request without access token fails on 401, Unauthorized
assertCurl 401 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_RECS -s"

## Verify that the reader - client with only read scope can call the read API but not delete API.
READER_ACCESS_TOKEN=$(curl -k https://reader:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=user -d password=password -s | jq .access_token -r)

##Testing with an OpenID Connect provider – Auth0
##SCOPE="movie:read"
##read $READER_ACCESS_TOKEN < <(./get-access-token.bash $SCOPE)
##AUTH="-H \"Authorization: Bearer $READER_ACCESS_TOKEN\""

READER_AUTH="-H \"Authorization: Bearer $READER_ACCESS_TOKEN\""

assertCurl 200 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_RECS $READER_AUTH -s"
assertCurl 403 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_RECS $READER_AUTH -X DELETE -s"

testCircuitBreaker

echo "End, all tests OK:" $(date)

if [[ $@ == *"stop"* ]]; then
  echo "Stopping the test environment..."
  echo "$ docker-compose down --remove-orphans"
  docker-compose down --remove-orphans
fi

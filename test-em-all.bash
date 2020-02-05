#!/usr/bin/env bash

# Sample usage:
#
#   HOST=localhost PORT=7000 ./test-em-all.bash
#
: ${HOST=localhost}
: ${PORT=8080}
: ${MOV_ID_REVS_RECS=2}
: ${MOV_ID_NOT_FOUND=14}
: ${MOV_ID_NO_RECS=114}
: ${MOV_ID_NO_REVS=214}

function assertCurl() {

    local expectedHttpCode=$1
    local curlCmd="$2 -w \"%{http_code}\""
    local result=$(eval $curlCmd)
    local httpCode="${result:(-3)}"
    RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

    if [ "$httpCode" = "$expectedHttpCode" ]
    then
        if [ "$httpCode" = "200" ]
        then
            echo "Test OK (HTTP Code: $httpCode)"
        else
            echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
        fi
        return 0
    else
        echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
        echo  "- Failing command: $curlCmd"
        echo  "- Response Body: $RESPONSE"
        return 1
    fi
}

function assertEqual() {

    local expected=$1
    local actual=$2

    if [ "$actual" = "$expected" ]
    then
        echo "Test OK (actual value: $actual)"
        return 0
    else
        echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
        return 1
    fi

        echo "Test OK (actual value: $actual)"
        return 0
}

function testUrl() {
    url=$@
    if $url -ks -f -o /dev/null
    then
          return 0
    else
          return 1
    fi;
}

function waitForService() {
    url=$@
    echo -n "Wait for: $url... "
    n=0
    until testUrl $url
    do
        n=$((n + 1))
        if [[ $n == 100 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 6
            echo -n ", retry #$n "
        fi
    done
}

function testCompositeCreated() {

    # Expect that the Movie Composite for movieId $MOV_ID_REVS_RECS has been created with three recommendations and three reviews
    if ! assertCurl 200 "curl http://$HOST:$PORT/movie-composite/$MOV_ID_REVS_RECS -s"
    then
        echo -n "FAIL"
        return 1
    fi

    set +e
    assertEqual "$MOV_ID_REVS_RECS" $(echo $RESPONSE | jq .movieId)
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    set -e
}

function waitForMessageProcessing() {
    echo "Wait for messages to be processed... "

    # Give background processing some time to complete...
    sleep 1

    n=0
    until testCompositeCreated
    do
        n=$((n + 1))
        if [[ $n == 40 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 6
            echo -n ", retry #$n "
        fi
    done
    echo "All messages are now processed!"
}

function recreateComposite() {
  local movieId=$1
  local composite=$2

    assertCurl 200 "curl -X DELETE http://$HOST:$PORT/movie-composite/${movieId} -s"
    curl -X POST http://$HOST:$PORT/movie-composite -H "Content-Type: application/json" --data "$composite"
}

function setupData() {


  body="{\"movieId\":$MOV_ID_REVS_RECS"
    body+=\
',"genre": "Science Fiction",
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
    body+=\
',"genre": "Science Fiction",
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
    body+=\
',"genre": "Science Fiction",
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


set -e

echo "Start Tests:" `date`

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then

    echo "Restarting the test environment..."

    echo "$ docker-compose down --remove-orphans"
    docker-compose down --remove-orphans
    echo "$ docker-compose up -d"
    docker-compose up -d
fi

waitForService curl http://$HOST:$PORT/actuator/health

setupData

waitForMessageProcessing

## Verify that a normal request works, expect three recommendations and three reviews
assertCurl 200 "curl http://$HOST:$PORT/movie-composite/$MOV_ID_REVS_RECS -s"

echo  "printing response"
$(echo "$RESPONSE" | jq)

assertEqual "$MOV_ID_REVS_RECS" $(echo $RESPONSE | jq .movieId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations  | length")
assertEqual 1 $(echo $RESPONSE | jq ".reviews | length")

# Verify that a 404 (Not Found) error is returned for a non existing movieId ($MOV_ID_NOT_FOUND)
assertCurl 404 "curl http://$HOST:$PORT/movie-composite/$MOV_ID_NOT_FOUND -s"

# Verify that no recommendations are returned for movieId $MOV_ID_NO_RECS
assertCurl 200 "curl http://$HOST:$PORT/movie-composite/$MOV_ID_NO_RECS -s"
assertEqual "$MOV_ID_NO_RECS" $(echo $RESPONSE | jq .movieId)
assertEqual 0 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

# Verify that no reviews are returned for movieId $MOV_ID_NO_REVS
assertCurl 200 "curl http://$HOST:$PORT/movie-composite/$MOV_ID_NO_REVS -s"
assertEqual $MOV_ID_NO_REVS $(echo $RESPONSE | jq .movieId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")

echo "End, all tests OK:" `date`

if [[ $@ == *"stop"* ]]
then
    echo "Stopping the test environment..."
    echo "$ docker-compose down --remove-orphans"
    docker-compose down --remove-orphans
fi




#    body=\
#'{"movieId":3331,"genre":"product 1","title":"lalalla", "recommendations":[
#        {"recommendationId":3211,"author":"author 1","rate":1,"content":"content 1"},
#        {"recommendationId":9911,"author":"author 2","rate":2,"content":"content 2"},
#        {"recommendationId":9931,"author":"author 3","rate":3,"content":"content 3"}
#    ], "reviews":[
#        {"reviewId":4511,"author":"author 1","subject":"subject 1","content":"content 1"},
#        {"reviewId":4711,"author":"author 2","subject":"subject 2","content":"content 2"},
#        {"reviewId":7511,"author":"author 3","subject":"subject 3","content":"content 3"}
#    ]}'

#    body=\
#'{"movieId":333,"genre":"product 1","title":"lalalla", "recommendations":[
#        {"recommendationId":321,"author":"author 1","rate":1,"content":"content 1"},
#        {"recommendationId":991,"author":"author 2","rate":2,"content":"content 2"},
#        {"recommendationId":993,"author":"author 3","rate":3,"content":"content 3"}
#    ], "reviews":[
#        {"reviewId":451,"author":"author 1","subject":"subject 1","content":"content 1"}
#    ]}'

#    body=\
#'{"movieId":334,"genre":"product 1","title":"lalalla", "recommendations":[], "reviews":[]}'

#    body=\
#'{"movieId":335,"genre":"product 1","title":"lalalla", "recommendations":[
#        {"recommendationId":321,"author":"author 1","rate":1,"content":"content 1"}
#    ], "reviews":[]}'

#    body=\
#'{"movieId":336,"genre":"product 1","title":"lalalla", "recommendations":[
#        {"recommendationId":3211,"author":"author 1","rate":1,"content":"content 1"}
#    ], "reviews":[
#        {"reviewId":4511,"author":"author 1","subject":"subject 1","content":"content 1"}
#    ]}'


docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --zookeeper zookeeper --list
set -eu
set -x

read  DOMAIN_NAME   \
      USER_EMAIL    \
      USER_PASSWORD \
      CLIENT_ID     \
      CLIENT_SECRET < <(../../Documents/creds.bash)

data() {
  local template='
{
  "grant_type":    "password",
  "username":      $username,
  "password":      $password,
  "audience":      "https://localhost:8443/movie-composite",
  "scope":         "openid email movie:read movie:write",
  "client_id":     $client_id,
  "client_secret": $client_secret
}'

  if jq <<<null -c \
    --arg username "${USER_EMAIL}" \
    --arg password "${USER_PASSWORD}" \
    --arg client_id "${CLIENT_ID}" \
    --arg client_secret "${CLIENT_SECRET}" \
    "$template"; then
    return
  else
    printf "ERROR: Can not format request data." >&2
    exit 1
  fi
}

post() {
  if curl --request POST \
    --url "https://${DOMAIN_NAME}/oauth/token" \
    --header 'content-type: application/json' \
    --data "$1" \
    -s; then
    return
  else
    printf "ERROR: Can not send post request." >&2
    exit 1
  fi
}

token() {
  if jq -r .access_token; then
    return
  else
    printf "ERROR: Can not parse JSON response." >&2
    exit 1
  fi
}

TOKEN="$(post "$(data)" | token)"



echo $TOKEN

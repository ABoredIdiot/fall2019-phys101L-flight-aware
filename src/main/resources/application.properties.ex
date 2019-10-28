# Search for XXX for things that need to be defined

logging.level.org.apache.http=INFO
spring.security.user.name= XXX

loop_interval_secs = 300

flightaware.username = XXX
flightaware.password = XXX
flightaware.base_url = https://flightxml.flightaware.com/json/FlightXML2


google.clientId=XXX
google.clientSecret=XXX

google.redirectUri=http://localhost:8080/google-login
google.accessTokenUri=https://www.googleapis.com/oauth2/v3/token
google.userAuthorizationUri=https://accounts.google.com/o/oauth2/auth
google.issuer=accounts.google.com
google.jwkUrl=https://www.googleapis.com/oauth2/v2/certs

admin_users=|XXX|

#db.host=XXX
db.host=127.0.0.1
db.port=5432
db.database=flights
db.username=postgres
db.password=XXX

db.driverClassName=org.postgresql.Driver
db.url=jdbc:postgresql://${db.host}:${db.port}/${db.database}
db.pool.initialSize=5

spring.datasource.type=org.apache.commons.dbcp2.BasicDataSource
spring.datasource.driverClassName=${db.driverClassName}
spring.datasource.url=${db.url}
spring.datasource.username=${db.username}
spring.datasource.password=${db.password}
spring.datasource.dbcp2.initial-size=${db.pool.initialSize}

aws.access_key = XXX
aws.secret_key = XXX+InaTtuk++U/ALclgRvPbKaKu+2Q

# SFO
#location_name=SFO
#location1=37.601258  -122.413616
#location2=37.636882 -122.291737
#maxNum=15

# Oglethorpe Small
#location_name=oglethorpe-small
#location1=33.869752 -84.341383
#location2=33.884966 -84.328385
#maxNum=105

# Oglethorpe and a bit (NE Atlanta)
location_name=oglethorpe-neighborhood
location1=33.819882 -84.360342
location2=33.904839 -84.257387
maxNum=105

# Metro Atlanta
#location_name=metro-atlanta
#location1=33.346089 -84.790950
#location2=33.958810 -84.110096
#maxNum=15
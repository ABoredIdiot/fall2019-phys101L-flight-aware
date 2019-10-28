create table flights
(
    flight_index SERIAL PRIMARY KEY,
    faFlightId TEXT NOT NULL,
    t TIMESTAMP NOT NULL,
    location POINT NOT NULL,
    flight_info_type TEXT,
    entry_created TIMESTAMP DEFAULT current_timestamp,
    flight_data JSONB NOT NULL,

    UNIQUE (faFlightId, t, flight_info_type)
)

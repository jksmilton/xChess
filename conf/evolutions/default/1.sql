# schema
 
# --- !Ups

CREATE TABLE "xusers" (
    xauthkey varchar(255),
    email varchar(255),
    oauthkey varchar(255),
    handle varchar(255),
    secret varchar(255),
    PRIMARY KEY (xauthkey)
);
 
-- CREATE SEQUENCE friendship_id_seq;
 
CREATE TABLE "friendships" (
	-- id integer NOT NULL DEFAULT nextval('friendship_id_seq'),
	id SERIAL,
	userone varchar(255) REFERENCES "xusers"(oauthkey),
	usertwo varchar(255) REFERENCES "xusers"(oauthkey),
	PRIMARY KEY (id)
);
 
-- CREATE SEQUENCE game_id_seq;

CREATE TABLE "games" (
	-- id integer NOT NULL DEFAULT nextval('game_id_seq'),
	id SERIAL,
	white varchar(255) REFERENCES "xusers"(oauthkey),
	black varchar(255) REFERENCES "xusers"(oauthkey),
	PRIMARY KEY (id)
);

-- CREATE SEQUENCE transcript_id_seq;

CREATE TABLE "transcripts" (
	-- id integer NOT NULL DEFAULT nextval('transcript_id_seq'),
	id SERIAL,
	game integer REFERENCES "games"(id),
	player varchar(255) REFERENCES "xusers"(oauthkey),
	move varchar(255),
	timePlayed timestamp, 
	PRIMARY KEY (id)
);
 
-- CREATE TABLE pending_friend_requests

CREATE TABLE "pending_friend_requests" (

	id SERIAL,
	requester varchar(255) REFERENCES "xusers"(oauthkey),
	requestee varchar(255) REFERENCES "xusers"(oauthkey),
	timeSubmitted timestamp,
	PRIMARY KEY (id)

);


CREATE TABLE "pending_game_requests" (

	id SERIAL,
	requester varchar(255) REFERENCES "xusers"(oauthkey),
	requestee varchar(255) REFERENCES "xusers"(oauthkey),
	timeSubmitted timestamp,
	PRIMARY KEY (id)

);

CREATE TABLE "application_ids" (

	appID varchar(255),
	contactEmail varchar(255),
	PRIMARY KEY (appID)
	
);

INSERT INTO "application_ids"(appID, contactEmail) values ('t%n08foxWLBRempWkv!O', 'jksmilton@gmail.com');

# --- !Downs
 
DROP TABLE "xusers";

DROP TABLE "friendships";
-- DROP SEQUENCE friendship_id_seq;

DROP TABLE "games";
-- DROP SEQUENCE xgame_id_seq;

DROP TABLE "transcripts";
-- DROP TABLE transcript_id_seq;

DROP TABLE "pending_friend_requests";

DROP TABLE "pending_game_requests";

DROP TABLE "application_ids";
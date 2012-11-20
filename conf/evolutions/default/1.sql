# schema
 
# --- !Ups

CREATE TABLE xusers (
    username varchar(255),
    email varchar(255),
    PRIMARY KEY (username)
);
 
-- CREATE SEQUENCE friendship_id_seq;
 
CREATE TABLE friendships (
	-- id integer NOT NULL DEFAULT nextval('friendship_id_seq'),
	id SERIAL,
	userone varchar(255) REFERENCES xusers(username),
	usertwo varchar(255) REFERENCES xusers(username),
	PRIMARY KEY (id)
);
 
-- CREATE SEQUENCE game_id_seq;

CREATE TABLE games (
	-- id integer NOT NULL DEFAULT nextval('game_id_seq'),
	id SERIAL,
	white varchar(255) REFERENCES xusers(username),
	black varchar(255) REFERENCES xusers(username),
	PRIMARY KEY (id)
);

-- CREATE SEQUENCE transcript_id_seq;

CREATE TABLE transcripts (
	-- id integer NOT NULL DEFAULT nextval('transcript_id_seq'),
	id SERIAL,
	game integer REFERENCES games(id),
	player varchar(255) REFERENCES xusers(username),
	PRIMARY KEY (id)
);
 
# --- !Downs
 
DROP TABLE xusers;

DROP TABLE friendships;
#DROP SEQUENCE friendship_id_seq;

DROP TABLE games;
-- DROP SEQUENCE xgame_id_seq;

DROP TABLE transcripts;
-- DROP TABLE transcript_id_seq;
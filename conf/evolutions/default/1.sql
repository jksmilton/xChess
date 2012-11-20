# schema
 
# --- !Ups
CREATE SEQUENCE friendship_id_seq;

CREATE TABLE users (
    username varchar(255),
    email varchar(255),
    PRIMARY KEY (username)
);
 

 
CREATE TABLE friendships (
	id integer NOT NULL DEFAULT nextval('friendship_id_seq'),
	userone varchar(255) REFERENCES users(username),
	usertwo varchar(255) REFERENCES users(username),
	PRIMARY KEY (id),
	ON DELETE CASCADE
);
 
CREATE SEQUENCE game_id_seq;

CREATE TABLE games (
	id integer NOT NULL DEFAULT nextval('game_id_seq'),
	white varchar(255) REFERENCES users(username),
	black varchar(255) REFERENCES users(username),
	PRIMARY KEY (id),
	ON DELETE CASCADE
);

CREATE SEQUENCE transcript_id_seq;

CREATE TABLE transcripts (
	id integer NOT NULL DEFAULT nextval('transcript_id_seq'),
	game integer REFERENCES games(id),
	player varchar(255) REFERENCES users(username),
	PRIMARY KEY (id),
	ON DELETE CASCADE
);
 
# --- !Downs
 
DROP TABLE users;

DROP TABLE friendships;
DROP SEQUENCE friendship_id_seq;

DROP TABLE games;
DROP SEQUENCE game_id_seq;

DROP TABLE transcripts;
DROP TABLE transcript_id_seq;
# schema
 
# --- !Ups

CREATE SEQUENCE user_id_seq;

CREATE TABLE users (
    id integer NOT NULL DEFAULT nextval('user_id_seq'),
    username varchar(255),
    email varchar(255)
);
 
CREATE SEQUENCE freindship_id_seq;
 
CREATE TABLE freindships (
	id integer NOT NULL DEFAULT nextval('freindship_id_seq'),
	userone integer,
	usertwo integer
);
 
CREATE SEQUENCE game_id_seq;

CREATE TABLE games (
	id integer NOT NULL DEFAULT nextval('game_id_seq'),
	white integer,
	black integer
);

CREATE SEQUENCE transcript_id_seq;

CREATE TABLE transcripts (
	id integer NOT NULL DEFAULT nextval('transcript_id_seq'),
	game integer,
	player integer
);
 
# --- !Downs
 
DROP TABLE users;
DROP SEQUENCE user_id_seq;

DROP TABLE freindships;
DROP SEQUENCE freindship_id_seq;

DROP TABLE games;
DROP SEQUENCE game_id_seq;

DROP TABLE transcripts;
DROP TABLE transcript_id_seq;
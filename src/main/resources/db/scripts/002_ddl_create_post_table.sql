BEGIN;


CREATE TABLE IF NOT EXISTS public.post
(
    id serial,
    name character varying(255),
    text text,
    link character varying(2048) NOT NULL,
    created timestamp without time zone,
    PRIMARY KEY (id),
    CONSTRAINT link UNIQUE (link)
);
END;
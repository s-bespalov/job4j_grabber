BEGIN;


CREATE TABLE IF NOT EXISTS public.rabbit
(
    id serial,
    created_date timestamp,
    PRIMARY KEY (id)
);
END;
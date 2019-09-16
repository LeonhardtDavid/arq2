-- !Ups

CREATE TABLE "user"
(
    id         SERIAL,
    name       VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- !Downs

DROP TABLE "user";

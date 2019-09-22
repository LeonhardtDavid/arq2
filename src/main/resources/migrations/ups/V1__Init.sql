CREATE TABLE "user"
(
    id         SERIAL,
    username   VARCHAR(50) UNIQUE NOT NULL,
    password   VARCHAR(255)       NOT NULL,
    first_name VARCHAR(50)        NOT NULL,
    last_name  VARCHAR(50)        NOT NULL,
    email      VARCHAR(255)       NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO "user"(username, password, first_name, last_name, email)
VALUES ('admin', '$2a$10$GgQPfzS73Pav.T.46OJUvuAatw/GjaiKCns/29b7Jh9ri9zb82BaO', 'Admin', 'Super', 'admin@test.com');

CREATE TABLE event
(
    id          SERIAL,
    name        VARCHAR(50)              NOT NULL,
    image       VARCHAR(500)             NOT NULL,
    description TEXT,
    date        TIMESTAMP WITH TIME ZONE NOT NULL,
    owner       INTEGER                  NOT NULL,
    tag         VARCHAR(50)              NOT NULL,
    public      BOOLEAN                  NOT NULL,
    street      VARCHAR(50)              NOT NULL,
    city        VARCHAR(50)              NOT NULL,
    country     VARCHAR(50)              NOT NULL,
    FOREIGN KEY (owner) REFERENCES "user" (id),
    PRIMARY KEY (id)
);

CREATE TABLE requirement
(
    id          SERIAL,
    event       INTEGER      NOT NULL,
    description VARCHAR(100) NOT NULL,
    quantity    SMALLINT     NOT NULL,
    FOREIGN KEY (event) REFERENCES event (id),
    PRIMARY KEY (id)
);

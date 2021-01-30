CREATE TABLE patients (
  id bigserial NOT NULL UNIQUE PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  birthDate DATE,
  address VARCHAR(255),
  gender VARCHAR(15),
  policy bigint
);

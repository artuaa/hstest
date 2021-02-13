CREATE TABLE patients (
  id bigserial NOT NULL UNIQUE PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  birthdate DATE,
  created DATETIME NOT NULL,
  updated DATETIME NOT NULL,
  address VARCHAR(255),
  gender VARCHAR(15),
  policy VARCHAR(255)
);

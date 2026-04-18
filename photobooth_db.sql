CREATE DATABASE photobooth_db;

USE photobooth_db;

CREATE TABLE users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    namaDepan    VARCHAR(100) NOT NULL,
    namaBelakang VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL DEFAULT 'user'
);
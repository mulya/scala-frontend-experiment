CREATE TABLE tags (
    id int AUTO_INCREMENT NOT NULL primary key,
    name varchar(255) NOT NULL
);

CREATE TABLE track_relations (
    track_id int NOT NULL,
    tag_id int NOT NULL
);
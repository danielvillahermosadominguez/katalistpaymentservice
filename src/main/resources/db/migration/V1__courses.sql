CREATE TABLE courses (
     course_id INTEGER NOT NULL,
     name TEXT NOT NULL DEFAULT '',
     price DECIMAL(12,2) NOT NULL
);

INSERT INTO courses (course_id, name, price)
VALUES
    (9,         'SAMPLE TEST - Moodle dev', 99.99),
    (27,         'SAMPLE TEST - Moodle prod', 66.99);



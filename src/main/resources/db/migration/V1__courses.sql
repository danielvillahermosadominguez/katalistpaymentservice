CREATE TABLE courses (
     courseId INTEGER NOT NULL,
     name TEXT NOT NULL DEFAULT '',
     prize DECIMAL(12,2) NOT NULL
);

INSERT INTO courses (courseId, name, prize)
VALUES
    (9,         'SAMPLE TEST - Moodle dev', 99.99),
    (27,         'SAMPLE TEST - Moodle prod', 66.99);



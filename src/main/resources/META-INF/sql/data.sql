CREATE TABLE IF NOT EXISTS Task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    dueDate DATE,
    completed BOOLEAN
);

INSERT INTO Task (title) VALUES ('This is the task-1');
INSERT INTO Task (title) VALUES ('This is the task-2');
INSERT INTO Task (title) VALUES ('This is the task-3');
INSERT INTO Task (title) VALUES ('This is the task-4');
INSERT INTO Task (title) VALUES ('This is the task-5');

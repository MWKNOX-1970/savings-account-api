-- Offensive terms are data, not code: they can be changed without a redeploy.

-- TODO (multi-language): add a "language" column (BCP-47 code) and make the unique key
CREATE TABLE offensive_nicknames (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    word VARCHAR(50) NOT NULL,
    CONSTRAINT uq_offensive_nicknames_word UNIQUE (word)
);

-- Placeholder seed data. TODO: replace with a maintained list.
INSERT INTO offensive_nicknames (word) VALUES
    ('potato'), ('turnip'), ('eejit'), ('numpty'), ('dumpling'), ('tube'), ('fool');

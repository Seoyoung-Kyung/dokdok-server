-- Migrate book_review.keyword_id to book_review_keyword join table.
CREATE TABLE IF NOT EXISTS book_review_keyword (
    book_review_keyword_id BIGSERIAL PRIMARY KEY,
    book_review_id BIGINT NOT NULL,
    keyword_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_book_review_keyword_review
        FOREIGN KEY (book_review_id) REFERENCES book_review (book_review_id),
    CONSTRAINT fk_book_review_keyword_keyword
        FOREIGN KEY (keyword_id) REFERENCES keyword (keyword_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_book_review_keyword
    ON book_review_keyword (book_review_id, keyword_id);

INSERT INTO book_review_keyword (book_review_id, keyword_id, created_at)
SELECT book_review_id, keyword_id, created_at
FROM book_review
WHERE keyword_id IS NOT NULL
ON CONFLICT DO NOTHING;

ALTER TABLE book_review DROP COLUMN IF EXISTS keyword_id;

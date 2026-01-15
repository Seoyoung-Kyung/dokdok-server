CREATE UNIQUE INDEX IF NOT EXISTS uq_book_review_active
    ON book_review (book_id, user_id)
    WHERE deleted_at IS NULL;
package com.dokdok.book.entity;

import com.dokdok.global.BaseTimeEntity;
import com.dokdok.keyword.entity.Keyword;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "book_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE book_review SET deleted_at = CURRENT_TIMESTAMP WHERE book_review_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class BookReview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @OneToMany(mappedBy = "bookReview", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookReviewKeyword> keywords = new ArrayList<>();

    public static BookReview create(Book book, User user, BigDecimal rating, List<Keyword> keywords) {
        BookReview review = BookReview.builder()
                .book(book)
                .user(user)
                .rating(rating)
                .build();
        review.replaceKeywords(keywords);
        return review;
    }

    public void updateReview(BigDecimal rating, List<Keyword> keywords) {
        this.rating = rating;
        replaceKeywords(keywords);
    }

    public void deleteReview() {
        this.markDeletedNow();
    }

    private void replaceKeywords(List<Keyword> keywords) {
        this.keywords = new ArrayList<>();
        for (Keyword keyword : keywords) {
            this.keywords.add(BookReviewKeyword.create(this, keyword));
        }
    }
}

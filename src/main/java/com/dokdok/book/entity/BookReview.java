package com.dokdok.book.entity;

import com.dokdok.global.BaseTimeEntity;
import com.dokdok.keyword.entity.Keyword;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "book_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    public static BookReview create(Book book, User user, BigDecimal rating, Keyword keyword) {
        return BookReview.builder()
                .book(book)
                .user(user)
                .rating(rating)
                .keyword(keyword)
                .build();
    }

    public void updateReview(BigDecimal rating, Keyword keyword) {
        this.rating = rating;
        this.keyword = keyword;
    }

    public void deleteReview() {
        this.markDeletedNow();
    }
}

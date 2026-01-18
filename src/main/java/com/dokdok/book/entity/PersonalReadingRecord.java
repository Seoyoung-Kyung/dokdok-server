package com.dokdok.book.entity;

import com.dokdok.global.BaseTimeEntity;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "personal_reading_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE personal_reading_record SET deleted_at = CURRENT_TIMESTAMP WHERE record_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PersonalReadingRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_book_id", nullable = false)
    private PersonalBook personalBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private RecordType recordType;

    @Column(name = "record_content", columnDefinition = "TEXT")
    private String recordContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    private Map<String, Object> meta;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;


    public static PersonalReadingRecord create(PersonalBook personalBook, User user, RecordType recordType, String recordContent, Map<String, Object> meta) {
        return PersonalReadingRecord.builder()
                .personalBook(personalBook)
                .user(user)
                .recordType(recordType)
                .recordContent(recordContent)
                .meta(meta)
                .build();

    }

    public void update(RecordType recordType, String recordContent, Map<String, Object> meta) {
        this.recordType = recordType;
        this.recordContent = recordContent;
        this.meta = meta;
    }
}

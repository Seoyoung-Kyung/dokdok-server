package com.dokdok.user.entity;

import com.dokdok.global.BaseTimeEntity;
import com.dokdok.oauth2.OAuth2UserInfo;
import com.dokdok.user.dto.request.UpdateUserInfoRequest;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "kakao_id", nullable = false)
    private Long kakaoId;

    public static User of(OAuth2UserInfo oAuth2UserInfo) {

        return User.builder()
                .kakaoId(oAuth2UserInfo.getId())
                .userEmail(oAuth2UserInfo.getEmail())
                .build();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 사용자의 정보를 변경합니다.
     * 현재는 닉네임만 가능.
     */
    public void updateInfo(UpdateUserInfoRequest request) {

        if (!this.nickname.equals(request.nickname())) {
            this.nickname = request.nickname();
        }
    }

    public void delete() {
        markDeletedNow();
    }

    public void updateProfileImage(String imageUrl) {
        this.profileImageUrl = imageUrl;
    }
}

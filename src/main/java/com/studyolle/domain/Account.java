package com.studyolle.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    //-- 회원가입 --//
    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime joinedAt; //가입날짜

    //-- 프로필 --//
    private String bio;

    private String url;

    private String occupation;

    private String location;    // varchar(255)

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    //-- 알림설정 --//
    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollResultByEmail;

    private boolean studyEnrollResultByWeb;

    private boolean studyUpdatedResultByEmail;

    private boolean studyUpdatedResultByWeb;

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }
}

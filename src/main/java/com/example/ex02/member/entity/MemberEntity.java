package com.example.ex02.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "MEMBER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq_gen")
    @SequenceGenerator(
            name = "member_seq_gen",
            sequenceName = "SEQ_MEMBER",
            allocationSize = 1
    )
    @Column(name = "USER_NO")
    private Long userNo;

    // ✔ 아이디: 영문+숫자 5~20자 (validation은 DTO에서 처리)
    @Column(name = "USER_ID", length = 20, nullable = false, unique = true)
    private String userId;

    // ✔ 비밀번호 해시값이 들어가므로 충분한 길이 확보
    @Column(name = "PASSWORD", length = 255, nullable = false)
    private String password;

    @Column(name = "NAME", length = 50, nullable = false)
    private String name;

    @Column(name = "EMAIL", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "PHONE", length = 20)
    private String phone;

    @Column(name = "GENDER", length = 1)
    private String gender;   // M / F

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Column(name = "JOIN_DATE")
    private LocalDate joinDate;

    @Column(name = "ROLE", length = 20)
    private String role;     // user / admin

    @Column(name = "IS_ACTIVE", length = 1)
    private String isActive; // Y / N

    @Column(name = "WITHDRAW_DATE")
    private LocalDate withdrawDate;

    // ✔ ‘자연/야간/체험…’ 등 preset 태그라 길이 100이면 충분
    @Column(name = "FAVORITE_TAG", length = 100)
    private String favoriteTag;
}

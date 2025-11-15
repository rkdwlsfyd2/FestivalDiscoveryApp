package com.example.ex02.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "USER_ID", length = 50, nullable = false, unique = true)
    private String userId;

    @Column(name = "PASSWORD", length = 255, nullable = false)
    private String password;

    @Column(name = "NAME", length = 50, nullable = false)
    private String name;

    @Column(name = "EMAIL", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "PHONE", length = 20)
    private String phone;

    @Column(name = "GENDER", length = 1)
    private String gender;   // 'M' or 'F'

    @Column(name = "BIRTH_DATE")
    private LocalDateTime birthDate;

    @Column(name = "JOIN_DATE")
    private LocalDateTime joinDate;

    @Column(name = "ROLE", length = 20)
    private String role;     // 'user', 'admin'

    @Column(name = "IS_ACTIVE", length = 1)
    private String isActive; // 'Y', 'N'

    @Column(name = "WITHDRAW_DATE")
    private LocalDateTime withdrawDate;

    @Column(name = "FAVORITE_TAG", length = 100)
    private String favoriteTag;
}

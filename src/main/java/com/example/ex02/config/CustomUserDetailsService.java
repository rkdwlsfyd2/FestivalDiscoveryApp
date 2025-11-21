package com.example.ex02.config;

import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        MemberEntity member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        return User.builder()
                .username(member.getUserId())
                .password(member.getPassword())         // BCrypt 암호화된 비밀번호
                .roles(member.getRole())                // user, admin
                .build();
    }
}

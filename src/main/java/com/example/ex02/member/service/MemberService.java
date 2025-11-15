package com.example.ex02.member.service;

import com.example.ex02.member.dto.MemberDto;
import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

}
package com.example.ex02.config.intercept;

import com.example.ex02.member.entity.MemberEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {    // 비로그인 상태라면 로그인 페이지로 이동
            response.sendRedirect("/login");
            return false;
        }else if (!"admin".equals(loginUser.getRole())){    // roll이 admin이 아니라면 홈페이지로 이동
            response.sendRedirect("/");
            return false;
        }

        return true;
    }
}

// admin과 관련된 url접근 시 해당 컨트롤러 접근 전 인터셉트
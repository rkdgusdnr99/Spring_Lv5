package com.example.postlv3.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Slf4j(topic = "LoggingFilter")
//@Component // spring security를 활용하지 않고 작성한 필터
@Order(1)
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 전처리
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String url = httpServletRequest.getRequestURI();
        String httpMethod = httpServletRequest.getMethod();
        long startTime = System.currentTimeMillis(); // 요청 처리 시작 시간
        log.info(url + " 다음 chain.doFilter -> AuthFilter 실행");

        // 다음 Filter 로 이동
        // AuthFilter에 인증 필요없이 api 요청을 했기 때문에 요청한 api url 컨트롤러로 이동해서 비즈니스 로직 수행
        chain.doFilter(request, response);

        // 후처리
        log.info("AUthFilter 실행 이후, ->비즈니스 로직 완료"); // 서비스 로직을 수행한 것

        long duration = System.currentTimeMillis() - startTime;
        int status = httpServletResponse.getStatus();

        log.info("{} - 요청 API URL {} - 상태코드: {} - 처리 시간: {}ms",httpMethod,url,status,duration);

    }
}
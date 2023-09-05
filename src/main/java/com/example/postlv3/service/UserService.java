package com.example.postlv3.service;

import com.example.postlv3.dto.SignupUserRequestDto;
import com.example.postlv3.dto.StatusResponseDto;
import com.example.postlv3.entity.User;
import com.example.postlv3.entity.UserRoleEnum;
import com.example.postlv3.jwt.JwtUtil;
import com.example.postlv3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    //회원가입 기능
    public StatusResponseDto signup(SignupUserRequestDto signupUserRequestDto) {
        String username = signupUserRequestDto.getUsername();
        String password = passwordEncoder.encode(signupUserRequestDto.getPassword());

        //회원 중복 확인
        Optional<User> checkUsername = userRepository.findByUsername(username);
        if (checkUsername.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자가 존재합니다.");
        }

        //사용자 role 확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (signupUserRequestDto.isAdmin()) {
            if (!ADMIN_TOKEN.equals(signupUserRequestDto.getAdminToken())) {
                throw new IllegalArgumentException("관리자 암호가 틀려 등록이 불가능합니다.");
            }
            role = UserRoleEnum.ADMIN;
        }

        //사용자 등록 =>입력한 이름과 암호화된 비밀번호 저장
        User user = new User(username,password,role);
        userRepository.save(user);

        StatusResponseDto signupUserResponse = new StatusResponseDto("회원가입 성공",200);

        return signupUserResponse;
    }


    //로그인 기능 -> security 분리 이전

    //spring security -> 적용 시 , jwt필터에서 처리하기 때문에 필요없어짐

//    public StatusResponseDto login(LoginUserRequestDto loginUserRequestDto, HttpServletResponse res) {
//        String username = loginUserRequestDto.getUsername();
//        String password = loginUserRequestDto.getPassword();
//
//        //사용자 확인
//        User user = userRepository.findByUsername(username).orElseThrow(
//                () -> new IllegalArgumentException("등록된 사용자가 없습니다.")
//        );
//
//        //비밀번호 확인
//        if (!passwordEncoder.matches(password, user.getPassword())) {
//            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
//        }
//
//        //JWT 생성 및 쿠키에 저장 후 res
//        String token = jwtUtil.createToken(user.getUsername(),user.getRole());
////        jwtUtil.addJwtToCookie(token,res);// 주석 처리
//        res.addHeader("Authorization",token); // -> header에 반환
//
//
//        StatusResponseDto loginUserResponse = new StatusResponseDto("로그인 성공", 200);
//        System.out.println("로그인 성공 " + "statusCode:200");
//        return loginUserResponse;
//
//    }


}

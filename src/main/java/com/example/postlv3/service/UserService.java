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

    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    //회원가입 기능
    public StatusResponseDto signup(SignupUserRequestDto signupUserRequestDto) {
        String username = signupUserRequestDto.getUsername();
        String password = passwordEncoder.encode(signupUserRequestDto.getPassword());


        //회원 중복 확인
        Optional<User> checkUsername = userRepository.findByUsername(username);
        if (checkUsername.isPresent()) {
            return new StatusResponseDto("중복된 username 입니다.",400);
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

}

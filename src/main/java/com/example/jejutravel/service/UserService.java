package com.example.jejutravel.service;

import com.example.jejutravel.config.TokenProvider;
import com.example.jejutravel.domain.Dto.SignInRequest;
import com.example.jejutravel.domain.Dto.SignInResponse;
import com.example.jejutravel.domain.Dto.SignUpRequest;
import com.example.jejutravel.domain.Dto.SignUpResponse;
import com.example.jejutravel.domain.Entity.RefreshToken;
import com.example.jejutravel.domain.Entity.User;
import com.example.jejutravel.repository.RefreshTokenRepository;
import com.example.jejutravel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Date;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByUserUsername(request.getUserUsername())) {
            return SignUpResponse.builder()
                    .msg("아이디가 이미 존재합니다.")
                    .build();
        }
        if (userRepository.existsByUserEmail(request.getUserEmail())) {
            return SignUpResponse.builder()
                    .msg("이메일이 이미 존재합니다.")
                    .build();
        }

        // 비밀번호 보안 규칙 확인
        if (!isValidPassword(request.getUserPassword())) {
            return SignUpResponse.builder()
                    .msg("비밀번호 형식이 올바르지 않습니다. 비밀번호는 8자 이상 16자 이하, 문자, 숫자, 특수문자를 포함해야 합니다.")
                    .build();
        }

        User user = User.builder()
                .userName(request.getUserName())
                .userDateOfBirth(request.getUserDateOfBirth())
                .userPhoneNumber(request.getUserPhoneNumber())
                .userGender(request.isUserGender())
                .userEmail(request.getUserEmail())
                .userUsername(request.getUserUsername())
                .userPassword(passwordEncoder.encode(request.getUserPassword()))
                .userCreatedAt(new Date(System.currentTimeMillis()))
                .userUpdatedAt(new Date(System.currentTimeMillis()))
                .build();

        userRepository.save(user);

        return SignUpResponse.builder()
                .msg("회원가입 성공")
                .build();
    }

    @Transactional
    public SignInResponse signIn(SignInRequest request) {
        User user = userRepository.findByUserUsername(request.getUserUsername());
        if (user == null) {
            return SignInResponse.builder()
                    .msg("아이디가 존재하지 않습니다.")
                    .build();
        }
        if (!passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
            return SignInResponse.builder()
                    .msg("비밀번호가 일치하지 않습니다.")
                    .build();
        }

        // User 객체를 기반으로 토큰을 생성합니다.
        String accessToken = tokenProvider.createToken(user);
        String refreshToken = tokenProvider.createRefreshToken();

        Optional<RefreshToken> oldRefreshToken = refreshTokenRepository.findById(user.getUserId());
        if (oldRefreshToken.isEmpty()) {
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .tokenId(user.getUserId())
                    .refreshToken(refreshToken)
                    .User(user)
                    .build();
            refreshTokenRepository.save(newRefreshToken);
        } else {
            RefreshToken newRefreshToken = oldRefreshToken.get().toBuilder()
                    .refreshToken(refreshToken)
                    .build();
            refreshTokenRepository.save(newRefreshToken);
        }

        return SignInResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .msg("로그인 성공")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public String signOut(String token) throws JsonProcessingException {
        String username = tokenProvider.validateTokenAndGetSubject(token);
        User user = userRepository.findByUserUsername(username);
        if (user == null || refreshTokenRepository.findById(user.getUserId()).isEmpty()) {
            return "로그아웃 실패";
        }
        try {
            refreshTokenRepository.deleteById(user.getUserId());
        } catch (Exception e) {
            return "로그아웃 실패";
        }
        return "로그아웃 성공";
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,16}$";
        return Pattern.matches(passwordPattern, password);
    }
}

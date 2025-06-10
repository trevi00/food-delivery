package com.portfolio.food_delivery.domain.user.service;

import com.portfolio.food_delivery.domain.user.dto.UserRegisterRequest;
import com.portfolio.food_delivery.domain.user.dto.UserResponse;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.entity.UserRole;
import com.portfolio.food_delivery.domain.user.exception.DuplicateEmailException;
import com.portfolio.food_delivery.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        // User 엔티티 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .role(UserRole.CUSTOMER)
                .build();

        // 저장
        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }
}
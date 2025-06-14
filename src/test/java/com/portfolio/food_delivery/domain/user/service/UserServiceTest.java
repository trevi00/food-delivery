package com.portfolio.food_delivery.domain.user.service;

import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.user.dto.LoginRequest;
import com.portfolio.food_delivery.domain.user.dto.LoginResponse;
import com.portfolio.food_delivery.domain.user.dto.UserRegisterRequest;
import com.portfolio.food_delivery.domain.user.dto.UserResponse;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.entity.UserRole;
import com.portfolio.food_delivery.domain.user.exception.DuplicateEmailException;
import com.portfolio.food_delivery.domain.user.exception.InvalidPasswordException;
import com.portfolio.food_delivery.domain.user.exception.UserNotFoundException;
import com.portfolio.food_delivery.domain.user.repository.UserRepository;
import com.portfolio.food_delivery.infrastructure.security.JwtTokenProvider;
import com.portfolio.food_delivery.infrastructure.security.TokenDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공")
    void registerUser_Success() {
        // given
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("test@example.com")
                .password("password123!")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // Reflection을 사용하여 ID 설정
            return User.builder()
                    .id(1L)
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .name(user.getName())
                    .phoneNumber(user.getPhoneNumber())
                    .role(user.getRole())
                    .build();
        });

        // when
        UserResponse response = userService.register(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("홍길동");

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 실패")
    void registerUser_DuplicateEmail_ThrowsException() {
        // given
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("existing@example.com")
                .password("password123!")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();

        given(userRepository.existsByEmail("existing@example.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        String email = "test@example.com";
        String rawPassword = "password123!";
        String encodedPassword = "encodedPassword";

        User user = User.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .role(UserRole.CUSTOMER)
                .build();

        LoginRequest request = new LoginRequest(email, rawPassword);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);
        given(jwtTokenProvider.createToken(any(TokenDto.class))).willReturn("jwt.token.here");

        // when
        LoginResponse response = userService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt.token.here");
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
        verify(jwtTokenProvider).createToken(any(TokenDto.class));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_UserNotFound_ThrowsException() {
        // given
        LoginRequest request = new LoginRequest("notfound@example.com", "password123!");

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository).findByEmail("notfound@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_InvalidPassword_ThrowsException() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");

        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
    }

    @Test
    @DisplayName("사용자 정보 조회 성공")
    void getUserInfo_Success() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .role(UserRole.CUSTOMER)
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUserInfo(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 - 존재하지 않는 사용자")
    void getUserInfo_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserInfo(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("이메일로 사용자 정보 조회 성공")
    void getUserInfoByEmail_Success() {
        // given
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .role(UserRole.CUSTOMER)
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUserInfoByEmail(email);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo("홍길동");

        verify(userRepository).findByEmail(email);
    }
}
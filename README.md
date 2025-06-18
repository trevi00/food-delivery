# 🍔 음식 배달 플랫폼 백엔드 개발 프로젝트

## 📋 프로젝트 개요

### 진행 기간
2025-06-11 ~ 진행중

### 프로젝트 소개
완전한 음식 배달 플랫폼의 백엔드 시스템을 Spring Boot 기반으로 설계 및 개발한 포트폴리오 프로젝트입니다. 
실제 서비스 수준의 아키텍처와 기능을 구현하여 대용량 트래픽 처리와 확장 가능한 시스템 설계를 했습니다.

### 주요 기능
- **다중 사용자 역할 기반 인증 시스템** (고객, 레스토랑 사장, 배달 파트너, 관리자)
- **실시간 주문 처리 및 상태 관리 시스템**
- **결제 시스템 통합** (PG사 연동 시뮬레이션)
- **리뷰 및 평점 시스템**
- **장바구니 관리 시스템**
- **RESTful API 설계 및 문서화**

## 🏗️ 시스템 아키텍처

### 아키텍처 설계
- **도메인 주도 설계(DDD)**: 비즈니스 로직을 명확히 분리하여 유지보수성 향상
- **계층형 아키텍처**: Presentation → Application → Domain → Infrastructure 계층 분리
- **REST API 설계**: RESTful 원칙을 준수한 직관적이고 일관성 있는 API 설계

### 보안 시스템
- **JWT 기반 인증/인가**: 무상태(stateless) 인증 시스템으로 확장성 확보
- **역할 기반 접근 제어(RBAC)**: 사용자 권한별 세밀한 접근 제어 구현
- **비밀번호 암호화**: BCrypt를 이용한 안전한 비밀번호 저장

### 데이터베이스 설계
- **JPA/Hibernate 활용**: 객체-관계 매핑을 통한 개발 생산성 향상
- **연관관계 최적화**: N+1 문제 해결을 위한 Fetch Join 설정
- **트랜잭션 관리**: 데이터 일관성 보장을 위한 선언적 트랜잭션 처리

## 🚀 기술 스택

### Backend
- **Java 17**: 최신 Java 기능 활용
- **Spring Boot 3.5.0**: 최신 Spring 생태계
- **Spring Security 6**: JWT 기반 보안
- **Spring Data JPA**: 데이터 접근 계층

### Database
- **H2 Database**: 개발/테스트 환경
- **MySQL**: 프로덕션 환경 (예정)
- **QueryDSL**: 타입 안전한 동적 쿼리

### Testing
- **JUnit 5**: 단위 테스트
- **Mockito**: Mock 테스트
- **MockMvc**: 통합 테스트
- **AssertJ**: Assertion 라이브러리

### Documentation
- **Swagger/OpenAPI 3.0**: API 문서 자동화
- **Spring REST Docs**: API 문서화 (예정)

## 📊 프로젝트 구조

```
src/
├── main/
│   └── java/com/portfolio/food_delivery/
│       ├── common/           # 공통 모듈
│       │   ├── entity/       # BaseEntity, Address
│       │   └── exception/    # 공통 예외
│       ├── domain/          # 도메인 계층
│       │   ├── user/        # 사용자 도메인
│       │   ├── restaurant/  # 레스토랑 도메인
│       │   ├── menu/        # 메뉴 도메인
│       │   ├── cart/        # 장바구니 도메인
│       │   ├── order/       # 주문 도메인
│       │   ├── payment/     # 결제 도메인
│       │   └── review/      # 리뷰 도메인
│       ├── infrastructure/  # 인프라 계층
│       │   ├── config/      # 설정 클래스
│       │   └── security/    # 보안 설정
│       └── presentation/    # 표현 계층
│           └── advice/      # 전역 예외 처리
└── test/                    # 테스트 코드
```

## 🔌 API 문서

### Swagger UI 접속
```
http://localhost:8080/swagger-ui.html
```

### API 엔드포인트 요약

#### 1. 사용자 API (`/api/users`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/register` | 회원가입 | No |
| POST | `/login` | 로그인 | No |
| GET | `/me` | 내 정보 조회 | Yes |

#### 2. 레스토랑 API (`/api/restaurants`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | 레스토랑 등록 | Yes (Owner) |
| GET | `/{id}` | 레스토랑 상세 조회 | No |
| GET | `/` | 레스토랑 목록 조회 | No |
| PUT | `/{id}` | 레스토랑 정보 수정 | Yes (Owner) |

#### 3. 메뉴 API
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/restaurants/{id}/menus` | 메뉴 등록 | Yes (Owner) |
| GET | `/api/restaurants/{id}/menus` | 메뉴 목록 조회 | No |
| PUT | `/api/menus/{id}` | 메뉴 수정 | Yes (Owner) |
| PATCH | `/api/menus/{id}/status` | 메뉴 상태 변경 | Yes (Owner) |
| DELETE | `/api/menus/{id}` | 메뉴 삭제 | Yes (Owner) |

#### 4. 장바구니 API (`/api/cart`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/items` | 장바구니에 추가 | Yes |
| GET | `/` | 장바구니 조회 | Yes |
| PUT | `/items/{menuId}` | 수량 변경 | Yes |
| DELETE | `/items/{menuId}` | 아이템 삭제 | Yes |
| DELETE | `/` | 장바구니 비우기 | Yes |

#### 5. 주문 API (`/api/orders`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | 주문 생성 | Yes |
| POST | `/from-cart` | 장바구니에서 주문 | Yes |
| GET | `/{id}` | 주문 상세 조회 | Yes |
| GET | `/my` | 내 주문 목록 | Yes |
| PATCH | `/{id}/status` | 주문 상태 변경 | Yes (Owner) |
| POST | `/{id}/cancel` | 주문 취소 | Yes |

#### 6. 결제 API (`/api/payments`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | 결제 처리 | Yes |
| GET | `/{id}` | 결제 정보 조회 | Yes |
| POST | `/{id}/cancel` | 결제 취소 | Yes |
| GET | `/history` | 결제 내역 | Yes |

#### 7. 리뷰 API (`/api/reviews`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | 리뷰 작성 | Yes |
| GET | `/{id}` | 리뷰 조회 | No |
| PUT | `/{id}` | 리뷰 수정 | Yes |
| DELETE | `/{id}` | 리뷰 삭제 | Yes |
| POST | `/{id}/reply` | 사장님 답변 | Yes (Owner) |

## 🧪 테스트

### 테스트 실행
```bash
./gradlew test
```

### 테스트 커버리지
- **단위 테스트**: 각 서비스 레이어의 비즈니스 로직 테스트
- **통합 테스트**: 컨트롤러부터 데이터베이스까지 전체 플로우 테스트
- **테스트 커버리지**: 핵심 비즈니스 로직 90% 이상

### 테스트 구성
- **20+ 테스트 클래스**
- **100+ 테스트 케이스**
- **Service Layer**: 비즈니스 로직 단위 테스트
- **Controller Layer**: MockMvc를 이용한 API 테스트
- **Integration Test**: 전체 플로우 통합 테스트

## 🚀 실행 방법

### 요구사항
- Java 17 이상
- Gradle 7.x 이상

### 빌드 및 실행
```bash
# 프로젝트 클론
git clone https://github.com/username/food-delivery.git
cd food-delivery

# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

###
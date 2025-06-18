package com.portfolio.food_delivery.domain.review.controller;

import com.portfolio.food_delivery.domain.review.dto.*;
import com.portfolio.food_delivery.domain.review.service.ReviewService;
import com.portfolio.food_delivery.domain.user.service.UserService;
import com.portfolio.food_delivery.infrastructure.security.SecurityUtil;
import com.portfolio.food_delivery.presentation.advice.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Reviews", description = "리뷰 관련 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @Operation(summary = "리뷰 작성",
            description = "배달 완료된 주문에 대해 리뷰를 작성합니다. 주문당 한 번만 작성 가능합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "리뷰 작성 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 리뷰 작성함, 배달 완료되지 않은 주문 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (주문자가 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Parameter(description = "리뷰 작성 정보", required = true)
            @Valid @RequestBody ReviewCreateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        ReviewResponse response = reviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "리뷰 상세 조회", description = "리뷰의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
            @Parameter(description = "리뷰 ID", required = true, example = "1")
            @PathVariable Long reviewId) {
        ReviewResponse response = reviewService.getReview(reviewId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 수정", description = "작성한 리뷰를 수정합니다. 작성자만 수정 가능합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (삭제된 리뷰)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @Parameter(description = "리뷰 ID", required = true, example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "리뷰 수정 정보", required = true)
            @Valid @RequestBody ReviewUpdateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        ReviewResponse response = reviewService.updateReview(reviewId, userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 삭제",
            description = "리뷰를 삭제합니다. 작성자만 삭제 가능하며, 실제로는 소프트 삭제됩니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "이미 삭제된 리뷰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "리뷰 ID", required = true, example = "1")
            @PathVariable Long reviewId) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사장님 답변 등록",
            description = "리뷰에 사장님 답변을 등록합니다. 레스토랑 소유자만 답변 가능합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "답변 등록 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "삭제된 리뷰에는 답변 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (레스토랑 소유자가 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewResponse> addReply(
            @Parameter(description = "리뷰 ID", required = true, example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "답변 내용", required = true)
            @Valid @RequestBody ReviewReplyRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        ReviewResponse response = reviewService.addReply(reviewId, userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "레스토랑 리뷰 목록 조회",
            description = "특정 레스토랑의 리뷰 목록을 최신순으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "레스토랑을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Page<ReviewResponse>> getRestaurantReviews(
            @Parameter(description = "레스토랑 ID", required = true, example = "1")
            @PathVariable Long restaurantId,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getRestaurantReviews(restaurantId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "레스토랑 리뷰 통계",
            description = "레스토랑의 리뷰 통계를 조회합니다. 평균 평점, 총 리뷰 수, 평점별 분포 등을 포함합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"averageRating\": 4.5,\n" +
                                            "  \"totalReviews\": 100,\n" +
                                            "  \"ratingDistribution\": {\n" +
                                            "    \"1\": 5,\n" +
                                            "    \"2\": 10,\n" +
                                            "    \"3\": 15,\n" +
                                            "    \"4\": 30,\n" +
                                            "    \"5\": 40\n" +
                                            "  }\n" +
                                            "}"
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "레스토랑을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/restaurants/{restaurantId}/stats")
    public ResponseEntity<Map<String, Object>> getRestaurantReviewStats(
            @Parameter(description = "레스토랑 ID", required = true, example = "1")
            @PathVariable Long restaurantId) {
        Map<String, Object> stats = reviewService.getRestaurantReviewStats(restaurantId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "내 리뷰 목록 조회",
            description = "현재 사용자가 작성한 리뷰 목록을 최신순으로 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/my")
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        Page<ReviewResponse> response = reviewService.getMyReviews(userId, pageable);
        return ResponseEntity.ok(response);
    }
}
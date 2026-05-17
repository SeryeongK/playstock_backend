package com.playstock.channel;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ChannelRegisterRequest {

    @NotBlank(message = "YouTube 채널 ID를 입력해주세요")
    private String youtubeChannelId;

    @NotNull(message = "카테고리를 선택해주세요")
    private ChannelCategory category;

    @Min(value = 1, message = "총 발행량은 1 이상이어야 합니다")
    private int totalShares;

    @Min(value = 1, message = "가격은 1 이상이어야 합니다")
    private int price;

    @Min(value = 1, message = "권리 기간은 1개월 이상이어야 합니다")
    private int durationMonths;

    @NotNull(message = "배당율을 입력해주세요")
    @DecimalMin(value = "0.01", message = "배당율은 0.01 이상이어야 합니다")
    @DecimalMax(value = "1.00", message = "배당율은 1.00 이하여야 합니다")
    private BigDecimal dividendRate;
}

package com.posty.postingapi.common.formatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DurationKoreanFormatterTest {

    @Test
    @DisplayName("Duration을 한글 문자열로 변환 테스트")
    void format_Success() {
        assertThat(DurationKoreanFormatter.format(Duration.ofMinutes(5))).isEqualTo("5분");
        assertThat(DurationKoreanFormatter.format(Duration.ofSeconds(90))).isEqualTo("1분 30초");
        assertThat(DurationKoreanFormatter.format(Duration.ofHours(1).plusMinutes(5))).isEqualTo("1시간 5분");
        assertThat(DurationKoreanFormatter.format(Duration.ZERO)).isEqualTo("0초");
        assertThat(DurationKoreanFormatter.format(Duration.ofMillis(230))).isEqualTo("0.23초");
    }

    @Test
    @DisplayName("소수점 자릿수 지정 테스트")
    void format_WithScale() {
        Duration duration = Duration.ofMillis(1234); // 1.234초
        assertThat(DurationKoreanFormatter.format(duration, 0)).isEqualTo("1초");
        assertThat(DurationKoreanFormatter.format(duration, 1)).isEqualTo("1.2초");
        assertThat(DurationKoreanFormatter.format(duration, 3)).isEqualTo("1.234초");
    }
}

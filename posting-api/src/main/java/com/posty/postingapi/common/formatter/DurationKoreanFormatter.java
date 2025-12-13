package com.posty.postingapi.common.formatter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link java.time.Duration}을 사람이 읽기 쉬운 한글 시간 문자열로 변환하는 유틸리티 클래스.
 *
 * <p>변환 규칙:
 * <ul>
 *   <li>Duration을 일 → 시간 → 분 → 초 순서로 분해하여 공백으로 연결한다.</li>
 *   <li>초 단위는 소수점 표현을 지원하며, 기본 소수점 자릿수는 {@value #DEFAULT_SECOND_SCALE}이다.</li>
 *   <li>{@code secondScale}은 0 이상 9 이하 범위로 보정되어 적용된다.</li>
 *   <li>1초 미만은 소수점 초로만 표시한다.</li>
 *   <li>정확히 나누어떨어지는 정수 초는 소수점 없이 표시한다.</li>
 *   <li>{@code null}, 0, 음수 Duration은 모두 {@code "0초"}로 처리한다.</li>
 * </ul>
 *
 * <p>소수점 자릿수 정책:
 * <ul>
 *   <li>{@code secondScale = 0} → 정수 초만 표시</li>
 *   <li>{@code secondScale = 2} → 소수점 둘째 자리까지 표시 (기본값)</li>
 *   <li>{@code secondScale > 9} → 나노초 정밀도를 초과하므로 9로 제한</li>
 * </ul>
 *
 * <p>출력 예시:
 * <pre>
 * Duration.ofMinutes(5)                       -> "5분"
 * Duration.ofSeconds(90)                      -> "1분 30초"
 * Duration.ofMillis(230)                      -> "0.23초"
 * Duration.ofHours(1).plusMinutes(5)          -> "1시간 5분"
 * Duration.ofSeconds(1)                       -> "1초"
 * Duration.ZERO                               -> "0초"
 *
 * format(duration, 0)                         -> "30초"
 * format(duration, 3)                         -> "30.123초"
 * </pre>
 *
 * <p>이 클래스는 상태를 가지지 않으며 모든 메서드는 정적(static)으로 제공된다.
 */
public final class DurationKoreanFormatter {

    private static final long SECONDS_PER_MINUTE = 60;
    private static final long SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE;
    private static final long SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;

    private static final int DEFAULT_SECOND_SCALE = 2;

    private DurationKoreanFormatter() {
    }

    public static String format(Duration duration) {
        return format(duration, DEFAULT_SECOND_SCALE);
    }

    public static String format(Duration duration, int secondScale) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return "0초";
        }

        long seconds = duration.getSeconds();
        int nanos = duration.getNano();

        List<String> parts = new ArrayList<>();

        seconds = extract(parts, seconds, SECONDS_PER_DAY, "일");
        seconds = extract(parts, seconds, SECONDS_PER_HOUR, "시간");
        seconds = extract(parts, seconds, SECONDS_PER_MINUTE, "분");

        if (seconds > 0 || nanos > 0) {
            double sec = seconds + nanos / 1_000_000_000.0;
            parts.add(formatSeconds(sec, Math.min(Math.max(secondScale, 0), 9)));
        }

        return String.join(" ", parts);
    }

    private static long extract(List<String> parts, long seconds, long unitSeconds, String label) {
        long value = seconds / unitSeconds;
        if (value > 0) {
            parts.add(value + label);
            seconds %= unitSeconds;
        }
        return seconds;
    }

    private static String formatSeconds(double seconds, int scale) {
        if (scale == 0) {
            return Math.round(seconds) + "초";
        }

        if (seconds < 1) {
            return String.format("%." + scale + "f초", seconds);
        }
        if (seconds == Math.floor(seconds)) {
            return ((long) seconds) + "초";
        }
        return String.format("%." + scale + "f초", seconds);
    }
}

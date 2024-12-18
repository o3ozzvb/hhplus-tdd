package io.hhplus.tdd.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCode {

    USER_ID_NOT_EXIST(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자 ID 입니다."),
    CHARGE_AMOUNT_LESS_THAN_ZERO(HttpStatus.BAD_REQUEST, "충전금액은 0보다 작을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

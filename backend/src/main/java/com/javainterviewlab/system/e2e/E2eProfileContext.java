package com.javainterviewlab.system.e2e;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/** 只在 e2e profile 中保存本次进程的隔离档案，不复用或删除历史档案。 */
@Component
@Profile("e2e")
public class E2eProfileContext {

    private final AtomicReference<Long> profileId = new AtomicReference<>();

    public void initialize(Long value) {
        if (!profileId.compareAndSet(null, value)) {
            throw new IllegalStateException("E2E 档案只能初始化一次");
        }
    }

    public Long requireProfileId() {
        Long value = profileId.get();
        if (value == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "E2E 学习档案尚未初始化");
        }
        return value;
    }
}

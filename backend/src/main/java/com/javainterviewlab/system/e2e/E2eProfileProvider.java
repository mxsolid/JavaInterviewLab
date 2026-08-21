package com.javainterviewlab.system.e2e;

import com.javainterviewlab.study.profile.service.CurrentProfileProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** e2e profile 下所有学习行为只写本次进程的新档案，生产默认档案不受影响。 */
@Primary
@Component
@Profile("e2e")
public class E2eProfileProvider implements CurrentProfileProvider {

    private final E2eProfileContext e2eProfileContext;

    public E2eProfileProvider(E2eProfileContext e2eProfileContext) {
        this.e2eProfileContext = e2eProfileContext;
    }

    @Override
    public Long requireProfileId() {
        return e2eProfileContext.requireProfileId();
    }
}

package com.javainterviewlab.study.progress;

import com.javainterviewlab.study.profile.service.CurrentProfileProvider;
import com.javainterviewlab.study.progress.repository.StudyProgressMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证进度事务锁只串行同一档案的同一道题。 */
@SpringBootTest
class StudyProgressLockIntegrationTest {

    private static final Duration QUICK_ASSERTION_TIMEOUT = Duration.ofMillis(500);
    private static final Duration COMPLETION_TIMEOUT = Duration.ofSeconds(5);

    @Autowired
    private StudyProgressMapper studyProgressMapper;

    @Autowired
    private CurrentProfileProvider currentProfileProvider;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 同题第二个事务必须等待第一个事务提交，防止两个旧快照同时参与掌握度计算。 */
    @Test
    void shouldSerializeSameQuestion() throws Exception {
        List<Long> questionIds = enabledQuestionIds();
        Long profileId = currentProfileProvider.requireProfileId();
        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondLocked = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> inTransaction(() -> {
                studyProgressMapper.lockProgress(profileId, questionIds.get(0));
                firstLocked.countDown();
                await(releaseFirst);
            }));
            assertThat(firstLocked.await(COMPLETION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)).isTrue();
            Future<?> second = executor.submit(() -> inTransaction(() -> {
                studyProgressMapper.lockProgress(profileId, questionIds.get(0));
                secondLocked.countDown();
            }));

            assertThat(secondLocked.await(QUICK_ASSERTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)).isFalse();
            releaseFirst.countDown();
            assertThat(secondLocked.await(COMPLETION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)).isTrue();
            first.get(COMPLETION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            second.get(COMPLETION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    /** 不同题目使用不同锁键，不能继续被整个 profile 的锁串行化。 */
    @Test
    void shouldAllowDifferentQuestionsInParallel() throws Exception {
        List<Long> questionIds = enabledQuestionIds();
        Long profileId = currentProfileProvider.requireProfileId();
        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondLocked = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> inTransaction(() -> {
                studyProgressMapper.lockProgress(profileId, questionIds.get(0));
                firstLocked.countDown();
                await(releaseFirst);
            }));
            assertThat(firstLocked.await(COMPLETION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)).isTrue();
            Future<?> second = executor.submit(() -> inTransaction(() -> {
                studyProgressMapper.lockProgress(profileId, questionIds.get(1));
                secondLocked.countDown();
            }));

            assertThat(secondLocked.await(COMPLETION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)).isTrue();
            releaseFirst.countDown();
            first.get(COMPLETION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            second.get(COMPLETION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    private List<Long> enabledQuestionIds() {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM question WHERE status = 'ENABLED' ORDER BY id LIMIT 2",
                Long.class
        );
        assertThat(ids).hasSize(2);
        return ids;
    }

    private void inTransaction(Runnable work) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> work.run());
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(COMPLETION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("等待并发测试信号超时");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("并发测试线程被中断", exception);
        }
    }
}

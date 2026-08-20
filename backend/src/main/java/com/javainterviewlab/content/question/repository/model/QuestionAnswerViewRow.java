package com.javainterviewlab.content.question.repository.model;

import java.time.Instant;
import java.util.UUID;

/** append-only 答案披露记录的只读投影。 */
public record QuestionAnswerViewRow(Long questionId, UUID clientViewId, Instant createdAt) {}

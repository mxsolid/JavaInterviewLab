export type AttemptResultType = 'NOT_ANSWERED' | 'WRONG' | 'PARTIAL' | 'CORRECT';

export interface StudyProgress {
  questionId: number;
  stage: string;
  stageDescription: string;
  masteryLevel: string;
  masteryDescription: string;
  attemptCount: number;
  wrongCount: number;
  wrongBookActive: boolean;
  lastStudiedAt: string | null;
  version: number | null;
}

export interface QuestionAttempt {
  id: number;
  questionId: number;
  clientAttemptId: string;
  viewedAnswer: boolean;
  selfRating: number | null;
  resultType: AttemptResultType;
  elapsedMs: number | null;
  createdAt: string;
}

export interface ScheduledReview {
  id: number;
  questionId: number;
  dueAt: string;
  status: string;
  statusDescription: string;
}

export interface SubmitAttemptRequest {
  questionId: number;
  clientAttemptId: string;
  answerText: string;
  viewedAnswer: boolean;
  selfRating?: number;
  resultType: AttemptResultType;
  elapsedMs: number;
}

export interface SubmitAttemptResponse {
  attempt: QuestionAttempt;
  progress: StudyProgress;
  review: ScheduledReview | null;
  duplicated: boolean;
}

export interface NoteData {
  id: number;
  targetType: 'QUESTION' | 'TOPIC' | 'SCENARIO';
  targetId: number;
  content: string;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export const studyQueryKeys = {
  dashboard: ['dashboard'] as const,
  question: (questionId: number) => ['content', 'question', questionId] as const,
  questionProgress: (questionId: number) => ['study', 'questions', questionId, 'progress'] as const,
  note: (targetType: 'QUESTION' | 'TOPIC', targetId: number) => ['study', 'notes', targetType, targetId] as const,
  plans: ['study', 'plans'] as const,
  currentPlan: ['study', 'current-plan'] as const,
  today: ['study', 'today'] as const,
  planDetail: (planId: number) => ['study', 'plans', planId] as const,
  wrongQuestions: ['study', 'wrong-questions'] as const,
  favorites: ['study', 'favorites'] as const,
  todayReviews: ['study', 'reviews', 'today'] as const,
  dueReviews: ['study', 'reviews', 'due'] as const,
};

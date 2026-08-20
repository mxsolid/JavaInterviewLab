export const studyQueryKeys = {
  plans: ['study', 'plans'] as const,
  currentPlan: ['study', 'current-plan'] as const,
  today: ['study', 'today'] as const,
  planDetail: (planId: number) => ['study', 'plans', planId] as const,
  wrongQuestions: ['study', 'wrong-questions'] as const,
  favorites: ['study', 'favorites'] as const,
  todayReviews: ['study', 'reviews', 'today'] as const,
};

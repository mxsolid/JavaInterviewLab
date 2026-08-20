export const studyQueryKeys = {
  plans: ['study', 'plans'] as const,
  currentPlan: ['study', 'current-plan'] as const,
  today: ['study', 'today'] as const,
  planDetail: (planId: number) => ['study', 'plans', planId] as const,
};

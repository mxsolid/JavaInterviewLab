export interface TechnicalTerm { key: string; matches: string[]; label: string; speechText: string; }

export const TECHNICAL_TERMS: TechnicalTerm[] = [
  { key: 'hash-map', matches: ['HashMap'], label: 'HashMap', speechText: 'Hash Map' },
  { key: 'thread-pool', matches: ['ThreadPoolExecutor', 'Thread Pool'], label: 'Thread Pool', speechText: 'Thread Pool Executor' },
  { key: 'transaction', matches: ['Transaction', '事务'], label: 'Transaction', speechText: 'Transaction' },
  { key: 'redis', matches: ['Redis'], label: 'Redis', speechText: 'Redis' },
  { key: 'jvm', matches: ['JVM'], label: 'JVM', speechText: 'Java Virtual Machine' },
  { key: 'spring', matches: ['Spring'], label: 'Spring', speechText: 'Spring Framework' },
];

export function findTechnicalTerms(text: string): TechnicalTerm[] {
  return TECHNICAL_TERMS.filter((term) => term.matches.some((match) => text.toLowerCase().includes(match.toLowerCase())));
}

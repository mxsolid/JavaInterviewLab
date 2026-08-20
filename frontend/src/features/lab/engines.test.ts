import { describe, expect, it } from 'vitest';
import { bPlusTreeInsertSteps, hashMapResizeSteps, lruSteps, redisRehashSteps, threadPoolSteps } from './engines';

describe('Lab step engines', () => {
  it('B+ tree 插入溢出后分裂并传播分隔键', () => {
    const steps = bPlusTreeInsertSteps([3, 8, 12, 17, 21, 30], 4, 28);
    expect(steps).toHaveLength(4);
    expect(steps.at(-1)?.state.metrics.phase).toBe('传播');
    expect(steps.at(-1)?.state.metrics.leafCount).toBe(3);
  });

  it('LRU 写满后淘汰最久未使用项', () => {
    const steps = lruSteps(3, [['A', 1], ['B', 2], ['C', 3]], ['GET:B', 'PUT:D:4']);
    expect(steps.at(-1)?.state.groups[0]?.items).toEqual(['D', 'B', 'A']);
  });

  it('HashMap 扩容完成全部节点迁移', () => {
    const steps = hashMapResizeSteps(8, [1, 5, 9, 13]);
    expect(steps.at(-1)?.state.metrics).toEqual({ capacity: 16, processed: 4 });
  });

  it('Redis rehash 最终清空旧表', () => {
    const steps = redisRehashSteps(8, [['A'], ['B', 'E'], [], ['D']]);
    expect(steps.at(-1)?.state.groups[0]?.items.every((item) => item.endsWith('∅'))).toBe(true);
  });

  it('ThreadPool 按核心线程、队列、最大线程、拒绝顺序处理', () => {
    const steps = threadPoolSteps(2, 4, 3, 8);
    expect(steps.at(-1)?.state.metrics).toMatchObject({ workers: 4, queued: 3, rejected: 1 });
  });
});

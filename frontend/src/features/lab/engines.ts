export interface LabStep<S> {
  state: S;
  explanation: string;
  highlighted?: string[];
}

export interface LabGroup {
  label: string;
  items: string[];
  tone?: 'blue' | 'green' | 'orange' | 'violet';
}

export interface LabVisualState {
  algorithm: string;
  groups: LabGroup[];
  metrics: Record<string, string | number>;
}

export interface LabDefinitionInput {
  algorithm?: string;
  initialDataset?: unknown;
  config?: unknown;
}

type JsonRecord = Record<string, unknown>;

const record = (value: unknown): JsonRecord => typeof value === 'object' && value !== null && !Array.isArray(value) ? value as JsonRecord : {};
const numbers = (value: unknown): number[] => Array.isArray(value) ? value.filter((item): item is number => typeof item === 'number') : [];
const integer = (value: unknown, fallback: number) => typeof value === 'number' && Number.isInteger(value) ? value : fallback;

export function buildLabSteps(definition: LabDefinitionInput, input?: number): LabStep<LabVisualState>[] {
  const initial = record(definition.initialDataset);
  const config = record(definition.config);
  switch (definition.algorithm) {
    case 'BPLUS_TREE_INSERT': return bPlusTreeInsertSteps(numbers(initial.keys), integer(initial.order, 4), input ?? integer(config.insertKey, 28));
    case 'LRU_CACHE': return lruSteps(integer(initial.capacity, 3), initial.entries, config.operations);
    case 'HASHMAP_RESIZE': return hashMapResizeSteps(integer(initial.capacity, 8), numbers(initial.hashes));
    case 'REDIS_REHASH': return redisRehashSteps(integer(initial.newSize, 8), initial.buckets);
    case 'THREAD_POOL_SUBMIT': return threadPoolSteps(integer(initial.core, 2), integer(initial.max, 4), integer(initial.queueCapacity, 3), integer(initial.tasks, 8));
    default: return [{ state: { algorithm: definition.algorithm ?? 'UNKNOWN', groups: [], metrics: {} }, explanation: '该实验暂未提供本地状态机。' }];
  }
}

export function bPlusTreeInsertSteps(keys: number[], order: number, insertKey: number): LabStep<LabVisualState>[] {
  const maxLeafKeys = Math.max(2, order - 1);
  const leaves = chunk([...keys].sort((a, b) => a - b), maxLeafKeys);
  const targetIndex = Math.max(0, leaves.findIndex((leaf) => insertKey <= (leaf.at(-1) ?? Number.MAX_SAFE_INTEGER)));
  const initial = treeState(leaves, insertKey, '定位');
  const insertedLeaves = leaves.map((leaf) => [...leaf]);
  const target = insertedLeaves[targetIndex] ?? [];
  target.push(insertKey);
  target.sort((a, b) => a - b);
  const inserted = treeState(insertedLeaves, insertKey, target.length > maxLeafKeys ? '溢出' : '完成');
  if (target.length <= maxLeafKeys) {
    return [
      { state: initial, explanation: `沿分隔键定位到第 ${targetIndex + 1} 个叶子节点。`, highlighted: [`leaf-${targetIndex}`] },
      { state: inserted, explanation: `按序插入 ${insertKey}，节点容量仍合法。`, highlighted: [String(insertKey)] },
    ];
  }
  const middle = Math.ceil(target.length / 2);
  const splitLeaves = insertedLeaves.flatMap((leaf, index) => index === targetIndex ? [leaf.slice(0, middle), leaf.slice(middle)] : [leaf]);
  return [
    { state: initial, explanation: `沿分隔键定位到第 ${targetIndex + 1} 个叶子节点。`, highlighted: [`leaf-${targetIndex}`] },
    { state: inserted, explanation: `按序插入 ${insertKey} 后叶子节点超过 ${maxLeafKeys} 个键。`, highlighted: [String(insertKey)] },
    { state: treeState(splitLeaves, insertKey, '分裂'), explanation: '把溢出叶子拆成两个有序节点，并保持叶子链顺序。', highlighted: [`leaf-${targetIndex}`, `leaf-${targetIndex + 1}`] },
    { state: treeState(splitLeaves, insertKey, '传播'), explanation: '将右侧新叶子的首键复制到父节点作为新的分隔键。', highlighted: [String(splitLeaves[targetIndex + 1]?.[0])] },
  ];
}

function treeState(leaves: number[][], insertKey: number, phase: string): LabVisualState {
  return {
    algorithm: 'BPLUS_TREE_INSERT',
    groups: [
      { label: '根分隔键', items: leaves.slice(1).map((leaf) => String(leaf[0])), tone: 'violet' },
      ...leaves.map((leaf, index) => ({ label: `叶子 ${index + 1}`, items: leaf.map(String), tone: 'green' as const })),
    ],
    metrics: { phase, insertKey, leafCount: leaves.length },
  };
}

export function lruSteps(capacity: number, rawEntries: unknown, rawOperations: unknown): LabStep<LabVisualState>[] {
  const entries = Array.isArray(rawEntries) ? rawEntries.filter(Array.isArray).map((item) => String(item[0])) : [];
  const operations = Array.isArray(rawOperations) ? rawOperations.map(String) : [];
  const order = [...entries];
  const steps: LabStep<LabVisualState>[] = [{ state: lruState(order, capacity, '初始'), explanation: '队首是最近使用项，队尾是最久未使用项。' }];
  for (const operation of operations) {
    const [type, key] = operation.split(':');
    const existing = order.indexOf(key);
    if (existing >= 0) order.splice(existing, 1);
    order.unshift(key);
    let evicted: string | undefined;
    if (order.length > capacity) evicted = order.pop();
    steps.push({
      state: lruState(order, capacity, operation),
      explanation: type === 'GET' ? `访问 ${key}，将它移动到队首。` : `写入 ${key}${evicted ? `，淘汰队尾 ${evicted}` : ''}。`,
      highlighted: [key, ...(evicted ? [evicted] : [])],
    });
  }
  return steps;
}

function lruState(order: string[], capacity: number, operation: string): LabVisualState {
  return { algorithm: 'LRU_CACHE', groups: [{ label: 'MRU → LRU', items: [...order], tone: 'blue' }], metrics: { capacity, operation, size: order.length } };
}

export function hashMapResizeSteps(capacity: number, hashes: number[]): LabStep<LabVisualState>[] {
  const nextCapacity = capacity * 2;
  const steps: LabStep<LabVisualState>[] = [{ state: hashState(capacity, hashes, 0), explanation: `旧数组容量为 ${capacity}，准备翻倍。` }];
  for (let index = 0; index < hashes.length; index += 1) {
    const processed = hashes.slice(0, index + 1);
    const hash = hashes[index];
    steps.push({
      state: hashState(nextCapacity, processed, index + 1),
      explanation: `(hash & oldCap) ${((hash & capacity) === 0) ? '= 0，保留原桶索引' : '!= 0，移动到原索引 + oldCap'}。`,
      highlighted: [String(hash)],
    });
  }
  return steps;
}

function hashState(capacity: number, hashes: number[], processed: number): LabVisualState {
  const buckets = new Map<number, string[]>();
  for (const hash of hashes) {
    const index = (capacity - 1) & hash;
    buckets.set(index, [...(buckets.get(index) ?? []), String(hash)]);
  }
  return { algorithm: 'HASHMAP_RESIZE', groups: [...buckets].map(([index, items]) => ({ label: `桶 ${index}`, items, tone: 'blue' })), metrics: { capacity, processed } };
}

export function redisRehashSteps(newSize: number, rawBuckets: unknown): LabStep<LabVisualState>[] {
  const oldBuckets = Array.isArray(rawBuckets) ? rawBuckets.map((bucket) => Array.isArray(bucket) ? bucket.map(String) : []) : [];
  const nextBuckets = Array.from({ length: newSize }, () => [] as string[]);
  const steps: LabStep<LabVisualState>[] = [{ state: redisState(oldBuckets, nextBuckets, 0), explanation: 'ht[0] 保存旧桶，ht[1] 已分配新空间。' }];
  oldBuckets.forEach((bucket, index) => {
    for (const key of bucket) nextBuckets[key.charCodeAt(0) % newSize]?.push(key);
    const migrated = oldBuckets.map((items, bucketIndex) => bucketIndex <= index ? [] : [...items]);
    steps.push({ state: redisState(migrated, nextBuckets, index + 1), explanation: `迁移旧桶 ${index}，rehashIndex 前进一位。`, highlighted: bucket });
  });
  return steps;
}

function redisState(oldBuckets: string[][], nextBuckets: string[][], rehashIndex: number): LabVisualState {
  return {
    algorithm: 'REDIS_REHASH',
    groups: [
      { label: 'ht[0] 旧表', items: oldBuckets.map((bucket, index) => `${index}: ${bucket.join(',') || '∅'}`), tone: 'orange' },
      { label: 'ht[1] 新表', items: nextBuckets.map((bucket, index) => `${index}: ${bucket.join(',') || '∅'}`), tone: 'green' },
    ],
    metrics: { rehashIndex },
  };
}

export function threadPoolSteps(core: number, max: number, queueCapacity: number, tasks: number): LabStep<LabVisualState>[] {
  let workers = 0;
  const queue: string[] = [];
  const rejected: string[] = [];
  const steps: LabStep<LabVisualState>[] = [{ state: poolState(workers, queue, rejected, core, max), explanation: '线程池尚未接收任务。' }];
  for (let task = 1; task <= tasks; task += 1) {
    const taskName = `T${task}`;
    let explanation: string;
    if (workers < core) {
      workers += 1;
      explanation = `${taskName} 创建核心线程执行。`;
    } else if (queue.length < queueCapacity) {
      queue.push(taskName);
      explanation = `${taskName} 进入工作队列。`;
    } else if (workers < max) {
      workers += 1;
      explanation = `${taskName} 创建非核心线程执行。`;
    } else {
      rejected.push(taskName);
      explanation = `${taskName} 触发拒绝策略。`;
    }
    steps.push({ state: poolState(workers, queue, rejected, core, max), explanation, highlighted: [taskName] });
  }
  return steps;
}

function poolState(workers: number, queue: string[], rejected: string[], core: number, max: number): LabVisualState {
  return {
    algorithm: 'THREAD_POOL_SUBMIT',
    groups: [
      { label: '工作线程', items: Array.from({ length: workers }, (_, index) => `Worker-${index + 1}`), tone: 'blue' },
      { label: '阻塞队列', items: [...queue], tone: 'violet' },
      { label: '已拒绝', items: [...rejected], tone: 'orange' },
    ],
    metrics: { workers, core, max, queued: queue.length, rejected: rejected.length },
  };
}

function chunk(values: number[], size: number): number[][] {
  const result: number[][] = [];
  for (let index = 0; index < values.length; index += size) result.push(values.slice(index, index + size));
  return result.length ? result : [[]];
}

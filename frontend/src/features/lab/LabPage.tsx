import { CaretRightOutlined, PauseOutlined, ReloadOutlined, StepBackwardOutlined, StepForwardOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { Button, Card, InputNumber, Select, Space, Tag, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { ErrorState, LoadingState } from '../../components/states';
import { PageHeader } from '../../components/ui/PageHeader';
import { SectionCard } from '../../components/ui/SectionCard';
import { labApi } from '../workspaces/api';
import { buildLabSteps } from './engines';

const speedOptions = [
  { value: 1400, label: '慢速' },
  { value: 900, label: '标准' },
  { value: 500, label: '快速' },
];

export function LabPage() {
  const labs = useQuery({ queryKey: ['v1', 'labs'], queryFn: labApi.list });
  const [code, setCode] = useState<string>();
  const [stepIndex, setStepIndex] = useState(0);
  const [input, setInput] = useState<number>();
  const [autoPlaying, setAutoPlaying] = useState(false);
  const [speed, setSpeed] = useState(900);

  useEffect(() => {
    if (!code && labs.data?.[0]?.code) setCode(labs.data[0].code);
  }, [code, labs.data]);

  const selected = labs.data?.find((item) => item.code === code);
  const steps = useMemo(() => selected ? buildLabSteps({ algorithm: selected.algorithm, initialDataset: selected.initialDataset, config: selected.config }, input) : [], [input, selected]);
  const current = steps[Math.min(stepIndex, Math.max(0, steps.length - 1))];

  useEffect(() => {
    setStepIndex(0);
    setAutoPlaying(false);
    const config = selected?.config;
    const insertKey = typeof config === 'object' && config !== null && 'insertKey' in config ? Number(config.insertKey) : undefined;
    setInput(Number.isFinite(insertKey) ? insertKey : undefined);
  }, [selected]);

  useEffect(() => {
    if (!autoPlaying) return undefined;
    const timer = window.setInterval(() => {
      setStepIndex((currentIndex) => {
        if (currentIndex >= steps.length - 1) {
          setAutoPlaying(false);
          return currentIndex;
        }
        return currentIndex + 1;
      });
    }, speed);
    return () => window.clearInterval(timer);
  }, [autoPlaying, speed, steps.length]);

  if (labs.isLoading) return <LoadingState />;
  if (labs.isError || !labs.data) return <ErrorState description="实验定义加载失败" />;
  if (!labs.data.length || !selected || !current) return <ErrorState description="暂无可运行实验" />;

  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <PageHeader title={`动画实验室 · ${selected.title ?? ''}`} description="算法由纯 TypeScript 状态机推进，页面只渲染当前状态和解释。" />
    <Card className="section-card lab-toolbar">
      <Space wrap>
        <Select aria-label="切换实验" value={code} onChange={setCode} options={labs.data.map((item) => ({ value: item.code, label: item.title }))} />
        <Tag color="green">{selected.versionLabel}</Tag>
        <Tag color="blue">{selected.algorithm}</Tag>
      </Space>
    </Card>

    <div className="lab-layout">
      <div className="lab-stage" aria-label="实验状态">
        <div className="lab-stage-content">
          <Space orientation="vertical" size={18} style={{ width: '100%' }}>
            <Typography.Title level={3} style={{ color: 'white', margin: 0 }}>{selected.description}</Typography.Title>
            <div className="lab-groups">{current.state.groups.map((group) => <div className={`lab-group lab-group-${group.tone ?? 'blue'}`} key={group.label}>
              <b>{group.label}</b>
              <div>{group.items.length ? group.items.map((item, index) => <span className={current.highlighted?.includes(item) ? 'active' : ''} key={`${item}-${index}`}>{item}</span>) : <span>∅</span>}</div>
            </div>)}</div>
            <Space wrap>{Object.entries(current.state.metrics).map(([key, value]) => <Tag key={key}>{key}: {String(value)}</Tag>)}</Space>
          </Space>
        </div>
      </div>
      <SectionCard title="演示控制" extra={<Tag color="blue">Step {stepIndex + 1} / {steps.length}</Tag>} className="lab-controls-card">
        <Space orientation="vertical" size={14} style={{ width: '100%' }}>
          <Space wrap>
            <Button icon={<ReloadOutlined />} onClick={() => { setStepIndex(0); setAutoPlaying(false); }}>重置</Button>
            <Button icon={<StepBackwardOutlined />} disabled={stepIndex === 0} onClick={() => setStepIndex((value) => Math.max(0, value - 1))}>上一步</Button>
            <Button type="primary" icon={<StepForwardOutlined />} disabled={stepIndex >= steps.length - 1} onClick={() => setStepIndex((value) => Math.min(steps.length - 1, value + 1))}>下一步</Button>
            <Button icon={autoPlaying ? <PauseOutlined /> : <CaretRightOutlined />} onClick={() => setAutoPlaying((value) => !value)}>{autoPlaying ? '暂停' : '自动'}</Button>
          </Space>
          <Select aria-label="播放速度" value={speed} onChange={setSpeed} options={speedOptions} />
          {selected.algorithm === 'BPLUS_TREE_INSERT' && <Space orientation="vertical" style={{ width: '100%' }}>
            <Typography.Text type="secondary">插入键值</Typography.Text>
            <InputNumber aria-label="插入键值" value={input} onChange={(value) => { setInput(value ?? undefined); setStepIndex(0); }} style={{ width: '100%' }} />
          </Space>}
          <SectionCard title="当前发生了什么"><Typography.Paragraph style={{ margin: 0 }}>{current.explanation}</Typography.Paragraph></SectionCard>
          <Typography.Text type="secondary">状态完全由第 {stepIndex + 1} 个 LabStep 生成；切换前后步骤不会写数据库。</Typography.Text>
        </Space>
      </SectionCard>
    </div>
  </Space>;
}

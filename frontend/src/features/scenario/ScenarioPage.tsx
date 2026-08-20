import { CheckCircleOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Button, Card, Col, Input, Radio, Rate, Row, Select, Space, Table, Tag, Typography } from 'antd';
import { useEffect, useMemo, useRef, useState } from 'react';
import { ErrorState, LoadingState } from '../../components/states';
import { PageHeader } from '../../components/ui/PageHeader';
import { SectionCard } from '../../components/ui/SectionCard';
import { StarRating } from '../../components/ui/StudyTags';
import { scenarioApi } from '../workspaces/api';

type ScenarioResult = 'NEEDS_WORK' | 'PARTIAL' | 'SOLID';

const resultLabels: Record<ScenarioResult, string> = { NEEDS_WORK: '需要加强', PARTIAL: '部分掌握', SOLID: '较熟练' };

export function ScenarioPage() {
  const queryClient = useQueryClient();
  const scenarios = useQuery({ queryKey: ['v1', 'scenarios'], queryFn: scenarioApi.list });
  const [scenarioId, setScenarioId] = useState<number>();
  const [caseId, setCaseId] = useState<number>();
  const [answer, setAnswer] = useState('');
  const [resultType, setResultType] = useState<ScenarioResult>('PARTIAL');
  const [rating, setRating] = useState(3);
  const startedAt = useRef(Date.now());
  const clientAttemptId = useRef(crypto.randomUUID());

  useEffect(() => {
    if (!scenarioId && scenarios.data?.[0]?.id) setScenarioId(scenarios.data[0].id);
  }, [scenarioId, scenarios.data]);

  const detail = useQuery({ queryKey: ['v1', 'scenarios', scenarioId], queryFn: () => scenarioApi.detail(scenarioId!), enabled: Boolean(scenarioId) });
  const matrix = useQuery({ queryKey: ['v1', 'scenarios', scenarioId, 'matrix'], queryFn: () => scenarioApi.matrix(scenarioId!), enabled: Boolean(scenarioId) });

  useEffect(() => {
    const firstCaseId = detail.data?.cases?.[0]?.id;
    if (firstCaseId && !detail.data?.cases?.some((item) => item.id === caseId)) setCaseId(firstCaseId);
  }, [caseId, detail.data]);

  const submit = useMutation({
    mutationFn: () => scenarioApi.submit({
      clientAttemptId: clientAttemptId.current,
      scenarioId: scenarioId!,
      caseId,
      answerText: answer,
      selfRating: rating,
      resultType,
      durationSeconds: Math.max(0, Math.round((Date.now() - startedAt.current) / 1000)),
    }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['v1', 'scenarios'] });
      clientAttemptId.current = crypto.randomUUID();
      startedAt.current = Date.now();
    },
  });

  const selectCase = (nextCaseId: number) => {
    setCaseId(nextCaseId);
    setAnswer('');
    setResultType('PARTIAL');
    setRating(3);
    submit.reset();
    clientAttemptId.current = crypto.randomUUID();
    startedAt.current = Date.now();
  };

  const selectedCase = detail.data?.cases?.find((item) => item.id === caseId);
  const caseCells = useMemo(() => (matrix.data?.cells ?? []).filter((item) => item.caseId === caseId), [caseId, matrix.data?.cells]);
  const candidateIds = new Set(caseCells.map((item) => item.solutionId));

  if (scenarios.isLoading) return <LoadingState />;
  if (scenarios.isError || !scenarios.data) return <ErrorState description="场景库加载失败" />;
  if (!scenarios.data.length) return <ErrorState description="场景库暂无启用内容" />;
  if (detail.isLoading || matrix.isLoading) return <LoadingState />;
  if (detail.isError || matrix.isError || !detail.data || !matrix.data) return <ErrorState description="场景工作区加载失败" />;

  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <PageHeader title={`场景训练 · ${detail.data.title ?? ''}`} description="先拆案例和不变量，再提交方案；候选关系和矩阵全部来自数据库。" />
    <Card className="section-card scenario-toolbar">
      <Space wrap>
        <Typography.Text strong>切换场景</Typography.Text>
        <Select value={scenarioId} onChange={setScenarioId} options={scenarios.data.map((item) => ({ value: item.id, label: item.title }))} />
        <StarRating value={detail.data.starLevel ?? 0} />
        <Tag color="blue">{detail.data.cases?.length ?? 0} Cases</Tag>
        <Tag>{detail.data.sourceVersion}</Tag>
      </Space>
    </Card>

    <div className="scenario-layout">
      <SectionCard title="CaseSwitcher" className="scenario-cases">
        {(detail.data.cases ?? []).map((item, index) => <button type="button" className={`scenario-case ${item.id === caseId ? 'active' : ''}`} key={item.id} onClick={() => item.id && selectCase(item.id)}>
          <b>Case {String.fromCharCode(65 + index)} · {item.title}</b>
          <span>{item.prompt}</span>
        </button>)}
      </SectionCard>

      <Space orientation="vertical" size={16} style={{ width: '100%' }}>
        <SectionCard title={selectedCase?.title ?? '案例'} extra={<Tag color="blue">{selectedCase?.code}</Tag>}>
          <div className="scenario-timeline">
            <div className="scenario-step"><i>1</i><div><b>现象与约束</b><p>{selectedCase?.prompt}</p></div></div>
            <div className="scenario-step"><i>2</i><div><b>根因边界</b><p>{selectedCase?.rootCause}</p></div></div>
            <div className="scenario-step"><i>3</i><div><b>给出可落地方案</b><p>说明最终不变量、并发防线、失败恢复和适用边界。</p></div></div>
          </div>
          <Input.TextArea aria-label="场景回答" value={answer} onChange={(event) => setAnswer(event.target.value)} rows={6} maxLength={20000} placeholder="先写你的分析和方案，再提交场景练习…" />
          <Space orientation="vertical" size={10} style={{ width: '100%', marginTop: 14 }}>
            <Radio.Group aria-label="场景掌握结果" value={resultType} onChange={(event) => setResultType(event.target.value)} options={(Object.keys(resultLabels) as ScenarioResult[]).map((value) => ({ value, label: resultLabels[value] }))} />
            <Space><Typography.Text>自评</Typography.Text><Rate value={rating} onChange={setRating} /></Space>
            {submit.isError && <Alert type="error" title="提交失败，当前输入仍保留" action={<Button size="small" onClick={() => submit.mutate()}>使用原编号重试</Button>} />}
            {submit.data && <Alert type="success" showIcon title={submit.data.duplicated ? '已恢复此前提交结果' : '场景练习已保存'} description={`结果：${submit.data.resultDescription ?? submit.data.resultType}`} />}
            <Button type="primary" icon={<ThunderboltOutlined />} loading={submit.isPending} disabled={!answer.trim()} onClick={() => submit.mutate()}>提交场景练习</Button>
          </Space>
        </SectionCard>
        {submit.data && (selectedCase?.expectedAnalysis ?? []).length > 0 && <SectionCard title="参考分析主线">
          <Space orientation="vertical">{selectedCase?.expectedAnalysis?.map((item) => <Typography.Text key={item}><CheckCircleOutlined className="success-icon" /> {item}</Typography.Text>)}</Space>
        </SectionCard>}
      </Space>

      <SectionCard title="候选方案" className="scenario-solutions">
        <Space orientation="vertical" size={10} style={{ width: '100%' }}>{(detail.data.solutions ?? []).map((solution) => <Card size="small" className={candidateIds.has(solution.id) ? 'solution-card recommended' : 'solution-card'} key={solution.id}>
          <Space orientation="vertical" size={5}>
            <Space wrap><Typography.Text strong>{solution.name}</Typography.Text>{candidateIds.has(solution.id) && <Tag color="green">当前 Case 候选</Tag>}</Space>
            <Typography.Text>{solution.principle}</Typography.Text>
            {solution.boundary && <Typography.Text type="secondary">边界：{solution.boundary}</Typography.Text>}
          </Space>
        </Card>)}</Space>
      </SectionCard>
    </div>

    <SectionCard title="方案对比矩阵（API 驱动）" extra={<Typography.Text type="secondary">{matrix.data.cells?.length ?? 0} 个关系单元</Typography.Text>}>
      <Table pagination={false} scroll={{ x: 'max-content' }} rowKey={(item) => item.id ?? item.code ?? ''} dataSource={matrix.data.cases ?? []} columns={[
        { title: 'Case / 方案', dataIndex: 'title', key: 'title', fixed: 'left', width: 220 },
        ...(matrix.data.solutions ?? []).map((solution) => ({
          title: solution.name,
          key: String(solution.id),
          width: 150,
          render: (_: unknown, caseItem: { id?: number }) => {
            const cell = matrix.data?.cells?.find((item) => item.caseId === caseItem.id && item.solutionId === solution.id);
            return cell ? <Tag color="green">{cell.recommendation ?? '候选'}</Tag> : <Typography.Text type="secondary">—</Typography.Text>;
          },
        })),
      ]} />
    </SectionCard>
  </Space>;
}

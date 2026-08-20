import { useRef, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Button, Card, Input, Radio, Rate, Space, Typography } from 'antd';
import { MarkdownRenderer } from '../../components/ui/MarkdownRenderer';
import { MasteryBadge } from '../../components/ui/StudyTags';
import { studyApi } from './api';
import { attemptResultLabel } from './labels';
import { studyQueryKeys } from './queryKeys';
import type { AttemptResultType, SubmitAttemptResponse } from './types';

type PracticePhase = 'READY' | 'ANSWERING' | 'REVEALED' | 'SUBMITTED';

interface PracticePanelProps {
  questionId: number;
  referenceAnswer?: string;
}

const resultTypes: AttemptResultType[] = ['NOT_ANSWERED', 'WRONG', 'PARTIAL', 'CORRECT'];

export function PracticePanel({ questionId, referenceAnswer }: PracticePanelProps) {
  const queryClient = useQueryClient();
  const [phase, setPhase] = useState<PracticePhase>('READY');
  const [answerText, setAnswerText] = useState('');
  const [resultType, setResultType] = useState<AttemptResultType>();
  const [selfRating, setSelfRating] = useState<number>();
  const [clientAttemptId, setClientAttemptId] = useState<string>();
  const startedAtRef = useRef<number | undefined>(undefined);
  const progress = useQuery({ queryKey: studyQueryKeys.questionProgress(questionId), queryFn: () => studyApi.getQuestionProgress(questionId) });
  const submit = useMutation({
    mutationFn: () => studyApi.submitAttempt({
      questionId,
      clientAttemptId: clientAttemptId!,
      answerText,
      viewedAnswer: true,
      selfRating,
      resultType: resultType!,
      elapsedMs: Math.max(0, Date.now() - (startedAtRef.current ?? Date.now())),
    }),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: studyQueryKeys.questionProgress(questionId) }),
        queryClient.invalidateQueries({ queryKey: studyQueryKeys.dashboard }),
        queryClient.invalidateQueries({ queryKey: studyQueryKeys.dueReviews }),
        queryClient.invalidateQueries({ queryKey: studyQueryKeys.wrongQuestions }),
      ]);
      setPhase('SUBMITTED');
    },
  });

  const start = () => {
    startedAtRef.current = Date.now();
    setClientAttemptId(crypto.randomUUID());
    setAnswerText('');
    setResultType(undefined);
    setSelfRating(undefined);
    submit.reset();
    setPhase('ANSWERING');
  };
  const canSubmit = Boolean(resultType && (resultType !== 'CORRECT' || selfRating));
  const result: SubmitAttemptResponse | undefined = submit.data;
  const visibleProgress = result?.progress ?? progress.data;

  return <Card className="section-card practice-panel" title="练习区">
    {phase === 'READY' && <Space direction="vertical" size={12}>
      <Typography.Text type="secondary">先组织自己的答案，再查看参考答案并完成自评。</Typography.Text>
      <Button type="primary" onClick={start}>开始练习</Button>
    </Space>}
    {phase !== 'READY' && phase !== 'SUBMITTED' && <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Input.TextArea value={answerText} onChange={(event) => setAnswerText(event.target.value)} rows={6} maxLength={10000} placeholder="写下你的回答。内容会随本次练习保存，便于后续回看。" />
      {phase === 'ANSWERING' && <Button type="primary" onClick={() => setPhase('REVEALED')}>查看参考答案</Button>}
      {phase === 'REVEALED' && <>
        <Card size="small" title="参考答案">{referenceAnswer ? <MarkdownRenderer content={referenceAnswer} /> : <Typography.Text type="secondary">本题暂未维护标准回答。</Typography.Text>}</Card>
        <div><Typography.Text strong>本次结果</Typography.Text><Radio.Group value={resultType} onChange={(event) => setResultType(event.target.value)} className="practice-result-group">{resultTypes.map((item) => <Radio.Button key={item} value={item}>{attemptResultLabel(item)}</Radio.Button>)}</Radio.Group></div>
        <div><Typography.Text strong>自评（1～5）{resultType === 'CORRECT' ? '，回答正确时必填' : '，可选'}</Typography.Text><Rate value={selfRating} onChange={setSelfRating} /></div>
        {submit.isError && <Alert type="error" showIcon message="提交失败，本次练习尚未丢失。" description="重试会使用同一个提交编号，不会重复推进进度。" />}
        <Space><Button type="primary" loading={submit.isPending} disabled={!canSubmit} onClick={() => submit.mutate()}>提交练习</Button>{submit.isError && <Button loading={submit.isPending} disabled={!canSubmit} onClick={() => submit.mutate()}>重试提交</Button>}</Space>
      </>}
    </Space>}
    {phase === 'SUBMITTED' && result && <Space direction="vertical" size={12} style={{ width: '100%' }}>
      <Alert type="success" showIcon message={result.duplicated ? '服务器已收到此前提交，本次已恢复原结果。' : '本次练习已保存'} />
      <Space wrap><span>当前掌握度：</span><MasteryBadge value={result.progress.masteryLevel} description={result.progress.masteryDescription} /><span>练习 {result.progress.attemptCount} 次</span><span>错误 {result.progress.wrongCount} 次</span></Space>
      <Typography.Text type="secondary">下次复习：{result.review ? new Date(result.review.dueAt).toLocaleString('zh-CN') : '当前无需安排'}</Typography.Text>
      <Button type="primary" onClick={start}>再练一次</Button>
    </Space>}
    {visibleProgress && phase === 'READY' && <Typography.Paragraph type="secondary" style={{ marginTop: 12, marginBottom: 0 }}>当前状态：{visibleProgress.stageDescription} · {visibleProgress.masteryDescription} · 已练习 {visibleProgress.attemptCount} 次</Typography.Paragraph>}
  </Card>;
}

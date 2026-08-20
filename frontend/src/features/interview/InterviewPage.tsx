import { AudioOutlined, CheckOutlined, StopOutlined } from '@ant-design/icons';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Alert, Button, Card, Col, Input, Row, Segmented, Select, Space, Statistic, Tag, Typography } from 'antd';
import { useRef, useState } from 'react';
import { PageHeader } from '../../components/ui/PageHeader';
import { SectionCard } from '../../components/ui/SectionCard';
import { contentApi } from '../content/api';
import { interviewApi, type InterviewFinish, type InterviewSession, type InterviewTurn } from '../workspaces/api';

interface SpeechResultEventLike {
  results: ArrayLike<{ 0: { transcript: string }; isFinal: boolean }>;
}

interface SpeechRecognitionLike {
  lang: string;
  continuous: boolean;
  interimResults: boolean;
  onresult: ((event: SpeechResultEventLike) => void) | null;
  onend: (() => void) | null;
  onerror: (() => void) | null;
  start(): void;
  stop(): void;
}

type SpeechRecognitionConstructor = new () => SpeechRecognitionLike;

function speechConstructor(): SpeechRecognitionConstructor | undefined {
  const target = window as unknown as { SpeechRecognition?: SpeechRecognitionConstructor; webkitSpeechRecognition?: SpeechRecognitionConstructor };
  return target.SpeechRecognition ?? target.webkitSpeechRecognition;
}

export function InterviewPage() {
  const topics = useQuery({ queryKey: ['content', 'topics'], queryFn: () => contentApi.topics() });
  const [mode, setMode] = useState<'RANDOM' | 'TOPIC'>('RANDOM');
  const [topicCode, setTopicCode] = useState<string>();
  const [session, setSession] = useState<InterviewSession>();
  const [prompt, setPrompt] = useState('');
  const [answer, setAnswer] = useState('');
  const [turn, setTurn] = useState<InterviewTurn>();
  const [finish, setFinish] = useState<InterviewFinish>();
  const [recording, setRecording] = useState(false);
  const clientTurnId = useRef(crypto.randomUUID());
  const recognitionRef = useRef<SpeechRecognitionLike | undefined>(undefined);
  const SpeechRecognition = speechConstructor();

  const create = useMutation({
    mutationFn: () => interviewApi.create(mode, mode === 'TOPIC' ? topicCode : undefined),
    onSuccess: (data) => {
      setSession(data);
      setPrompt(data.prompt ?? '');
      setAnswer('');
      setTurn(undefined);
      setFinish(undefined);
      clientTurnId.current = crypto.randomUUID();
    },
  });
  const submit = useMutation({
    mutationFn: () => interviewApi.submit(session!.id!, clientTurnId.current, answer),
    onSuccess: setTurn,
  });
  const finishMutation = useMutation({
    mutationFn: () => interviewApi.finish(session!.id!),
    onSuccess: setFinish,
  });

  const continueInterview = () => {
    if (!turn?.nextPrompt) return;
    setPrompt(turn.nextPrompt);
    setAnswer('');
    setTurn(undefined);
    clientTurnId.current = crypto.randomUUID();
  };

  const toggleSpeech = () => {
    if (!SpeechRecognition) return;
    if (recording) {
      recognitionRef.current?.stop();
      return;
    }
    const recognition = new SpeechRecognition();
    recognition.lang = 'zh-CN';
    recognition.continuous = true;
    recognition.interimResults = false;
    recognition.onresult = (event) => {
      const transcript = Array.from(event.results).filter((result) => result.isFinal).map((result) => result[0].transcript).join('');
      if (transcript) setAnswer((value) => `${value}${value ? '\n' : ''}${transcript}`);
    };
    recognition.onend = () => setRecording(false);
    recognition.onerror = () => setRecording(false);
    recognitionRef.current = recognition;
    setRecording(true);
    recognition.start();
  };

  const dimensions = finish?.dimensions ?? turn?.dimensions ?? [];
  const totalScore = finish?.totalScore ?? turn?.totalScore;

  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <PageHeader title="模拟面试" description="文本输入始终可用；本地规则评分不依赖 AI Key，每个维度都给出评分依据。" />

    {!session && <SectionCard title="开始一轮面试">
      <Space orientation="vertical" size={14} style={{ width: '100%' }}>
        <Segmented value={mode} options={[{ value: 'RANDOM', label: '题库随机' }, { value: 'TOPIC', label: '指定专题' }]} onChange={(value) => setMode(value as 'RANDOM' | 'TOPIC')} />
        {mode === 'TOPIC' && <Select aria-label="面试专题" showSearch optionFilterProp="label" value={topicCode} onChange={setTopicCode} options={topics.data?.map((item) => ({ value: item.code, label: item.name }))} placeholder="选择专题" />}
        {create.isError && <Alert type="error" title="面试会话创建失败" description="请检查当前专题是否有启用题目。" />}
        <Button type="primary" loading={create.isPending} disabled={mode === 'TOPIC' && !topicCode} onClick={() => create.mutate()}>开始模拟面试</Button>
      </Space>
    </SectionCard>}

    {session && <>
      <Row gutter={[16, 16]} className="interview-layout">
        <Col xs={24} lg={12}>
          <Card className="interviewer-card">
            <Space orientation="vertical" size={14}>
              <Space wrap><div className="interviewer-avatar">J</div><div><Typography.Text className="interviewer-name">Java 面试官</Typography.Text><div><Tag color="blue">第 {turn?.sequenceNo ?? session.sequenceNo ?? 1} 轮</Tag><Tag>{session.provider}</Tag>{session.providerEnabled ? <Tag color="violet">Provider flag ON</Tag> : <Tag>本地规则</Tag>}</div></div></Space>
              <Typography.Title level={3} className="interviewer-prompt">{prompt}</Typography.Title>
              <Typography.Text className="interviewer-hint">回答应包含核心结论、机制依据、边界条件和一个业务例子。</Typography.Text>
            </Space>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <SectionCard title="你的回答" extra={SpeechRecognition && <Button icon={recording ? <StopOutlined /> : <AudioOutlined />} onClick={toggleSpeech}>{recording ? '停止录音' : '语音输入'}</Button>}>
            <Space orientation="vertical" size={12} style={{ width: '100%' }}>
              <Input.TextArea aria-label="面试回答" value={answer} onChange={(event) => setAnswer(event.target.value)} rows={9} maxLength={20000} disabled={Boolean(turn)} placeholder="用自己的话回答。文本输入不依赖浏览器语音能力。" />
              {recording && <div className="interview-wave" aria-label="正在录音">{Array.from({ length: 5 }, (_, index) => <span key={index} />)}</div>}
              {submit.isError && <Alert type="error" title="回答提交失败" description="当前文字仍保留，重试使用同一个轮次编号。" />}
              {!turn && <Button type="primary" icon={<CheckOutlined />} loading={submit.isPending} disabled={!answer.trim()} onClick={() => submit.mutate()}>提交并评分</Button>}
              {turn && <Space wrap>
                {turn.nextPrompt && <Button onClick={continueInterview}>继续追问</Button>}
                <Button type="primary" loading={finishMutation.isPending} onClick={() => finishMutation.mutate()}>结束并汇总</Button>
              </Space>}
            </Space>
          </SectionCard>
        </Col>
      </Row>

      {totalScore !== undefined && <SectionCard title="可解释评分" extra={<Tag color="blue">Rule-based baseline</Tag>}>
        {finish && <Alert type="success" showIcon title={`面试已结束 · ${finish.totalScore ?? 0} 分`} description={finish.summary} />}
        <Row gutter={[12, 12]} className="score-grid">{dimensions.map((item) => <Col xs={12} lg={6} key={item.code}>
          <Card size="small" className="score-card">
            <Statistic title={item.label} value={item.score ?? 0} suffix={`/ ${item.maxScore ?? 0}`} precision={2} />
            <Typography.Paragraph type="secondary" style={{ margin: '8px 0 0' }}>{item.reason}</Typography.Paragraph>
          </Card>
        </Col>)}</Row>
        {!finish && <Typography.Paragraph type="secondary" style={{ marginTop: 14 }}>本轮总分：{totalScore} / 100。评分只依据数据库 rubric 与可解释文本规则，不声称等同人工面试官判断。</Typography.Paragraph>}
      </SectionCard>}
    </>}
  </Space>;
}

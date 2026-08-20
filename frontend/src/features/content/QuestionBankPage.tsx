import { EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Button, Card, Col, Form, Input, InputNumber, Modal, Row, Select, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ErrorState, LoadingState } from '../../components/states';
import { contentApi, type QuestionDetail, type QuestionPayload, type QuestionSummary } from './api';

type Filter = { keyword?: string; categoryId?: number; topicId?: number; starLevel?: number; difficulty?: string; frequencyLevel?: string; status?: string; page: number; pageSize: number; };
const answerTypes = ['QUICK_30S', 'STANDARD', 'DEEP'];

function QuestionEditor({ value, onClose }: { value?: QuestionDetail; onClose: () => void }) {
  const [form] = Form.useForm(); const client = useQueryClient();
  const setup = useQuery({ queryKey: ['content-options'], queryFn: async () => ({ topics: await contentApi.topics(), tags: await contentApi.tags() }) });
  const initial = value && { ...value, tagIds: value.tags.map((tag) => tag.id), quick: value.answers.find((answer) => answer.answerType === 'QUICK_30S')?.content, standard: value.answers.find((answer) => answer.answerType === 'STANDARD')?.content, deep: value.answers.find((answer) => answer.answerType === 'DEEP')?.content, followUpsText: value.followUps.map((item) => item.title).join('\n') };
  const save = async () => {
    const fields = await form.validateFields();
    const answers = answerTypes.map((answerType, sortOrder) => ({ answerType, content: fields[answerType.toLowerCase().replace('_30s', '')], sortOrder })).filter((item) => item.content?.trim());
    const payload: QuestionPayload = { topicId: fields.topicId, title: fields.title, questionType: fields.questionType, starLevel: fields.starLevel, difficulty: fields.difficulty, frequencyLevel: fields.frequencyLevel, originType: fields.originType, status: fields.status, oneLiner: fields.oneLiner, plainExplanation: fields.plainExplanation, designReason: fields.designReason, commonMistakes: fields.commonMistakes, scorePoints: fields.scorePoints, version: value?.version, tagIds: fields.tagIds ?? [], answers, followUps: (fields.followUpsText ?? '').split('\n').map((title: string, sortOrder: number) => ({ title: title.trim(), sortOrder })).filter((item: { title: string }) => item.title) };
    if (value && payload.version !== undefined) await contentApi.updateQuestion(value.id, payload as QuestionPayload & { version: number }); else { const { version: _, ...createPayload } = payload; await contentApi.createQuestion(createPayload); }
    await client.invalidateQueries({ queryKey: ['questions'] }); message.success('题目已保存'); onClose();
  };
  return <Modal open title={value ? '编辑题目' : '新增题目'} width={900} okText="保存" onCancel={onClose} onOk={() => void save()} destroyOnHidden>
    <Form form={form} layout="vertical" initialValues={{ questionType: 'KNOWLEDGE', starLevel: 3, difficulty: 'MEDIUM', frequencyLevel: 'HIGH', originType: 'USER', status: 'ENABLED', ...initial }}>
      <Row gutter={16}><Col span={12}><Form.Item name="title" label="题目标题" rules={[{ required: true, message: '请输入题目标题' }]}><Input /></Form.Item></Col><Col span={12}><Form.Item name="topicId" label="所属专题" rules={[{ required: true, message: '请选择专题' }]}><Select loading={setup.isLoading} options={setup.data?.topics.filter((item) => item.status === 'ENABLED').map((item) => ({ value: item.id, label: `${item.categoryName} / ${item.name}` }))} /></Form.Item></Col></Row>
      <Row gutter={16}><Col span={6}><Form.Item name="starLevel" label="星级"><InputNumber min={1} max={5} style={{ width: '100%' }} /></Form.Item></Col><Col span={6}><Form.Item name="difficulty" label="难度"><Select options={['EASY', 'MEDIUM', 'HARD'].map((value) => ({ value }))} /></Form.Item></Col><Col span={6}><Form.Item name="frequencyLevel" label="高频度"><Select options={['LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH'].map((value) => ({ value }))} /></Form.Item></Col><Col span={6}><Form.Item name="status" label="状态"><Select options={['ENABLED', 'DISABLED'].map((value) => ({ value }))} /></Form.Item></Col></Row>
      <Form.Item name="tagIds" label="标签"><Select mode="multiple" options={setup.data?.tags.map((tag) => ({ value: tag.id, label: tag.name }))} /></Form.Item>
      <Form.Item name="oneLiner" label="一句话理解"><Input.TextArea rows={2} /></Form.Item><Form.Item name="plainExplanation" label="通俗讲解（Markdown）"><Input.TextArea rows={4} /></Form.Item><Form.Item name="designReason" label="为什么这样设计（Markdown）"><Input.TextArea rows={4} /></Form.Item>
      <Row gutter={16}><Col span={8}><Form.Item name="quick" label="30 秒回答"><Input.TextArea rows={4} /></Form.Item></Col><Col span={8}><Form.Item name="standard" label="标准回答"><Input.TextArea rows={4} /></Form.Item></Col><Col span={8}><Form.Item name="deep" label="深入回答"><Input.TextArea rows={4} /></Form.Item></Col></Row>
      <Form.Item name="commonMistakes" label="常见错误 / 易错点"><Input.TextArea rows={3} /></Form.Item><Form.Item name="scorePoints" label="面试得分点"><Input.TextArea rows={3} /></Form.Item><Form.Item name="followUpsText" label="高频追问（每行一项）"><Input.TextArea rows={4} /></Form.Item>
    </Form>
  </Modal>;
}

export function QuestionBankPage() {
  const [filter, setFilter] = useState<Filter>({ page: 1, pageSize: 10, status: 'ENABLED' }); const [editing, setEditing] = useState<QuestionDetail | undefined>(); const [editorOpen, setEditorOpen] = useState(false); const navigate = useNavigate();
  const categories = useQuery({ queryKey: ['categories'], queryFn: contentApi.categories }); const topics = useQuery({ queryKey: ['topics', filter.categoryId], queryFn: () => contentApi.topics(filter.categoryId) });
  const questions = useQuery({ queryKey: ['questions', filter], queryFn: () => contentApi.questions(filter) });
  const openEdit = async (id: number) => { setEditing(await contentApi.question(id)); setEditorOpen(true); };
  const columns: ColumnsType<QuestionSummary> = [{ title: '题目', dataIndex: 'title', render: (title, row) => <Button type="link" onClick={() => navigate(`/questions/${row.id}`)}>{title}</Button> }, { title: '专题', dataIndex: 'topicName' }, { title: '星级', dataIndex: 'starLevel', render: (value) => '★'.repeat(value) }, { title: '难度', dataIndex: 'difficulty', render: (value) => <Tag color="blue">{value}</Tag> }, { title: '高频度', dataIndex: 'frequencyLevel', render: (value) => <Tag color="purple">{value}</Tag> }, { title: '操作', render: (_, row) => <Button icon={<EditOutlined />} onClick={() => void openEdit(row.id)}>编辑</Button> }];
  if (questions.isLoading) return <LoadingState />; if (questions.isError || !questions.data) return <ErrorState description="题库加载失败" />;
  return <Space direction="vertical" size={20} style={{ width: '100%' }}>
    <Card><Space wrap><Input prefix={<SearchOutlined />} placeholder="搜索标题或一句话理解" value={filter.keyword} onChange={(event) => setFilter({ ...filter, keyword: event.target.value, page: 1 })} style={{ width: 240 }} /><Select allowClear placeholder="分类" value={filter.categoryId} onChange={(categoryId) => setFilter({ ...filter, categoryId, topicId: undefined, page: 1 })} options={categories.data?.map((item) => ({ value: item.id, label: item.name }))} style={{ width: 150 }} /><Select allowClear placeholder="专题" value={filter.topicId} onChange={(topicId) => setFilter({ ...filter, topicId, page: 1 })} options={topics.data?.map((item) => ({ value: item.id, label: item.name }))} style={{ width: 180 }} /><Select allowClear placeholder="星级" value={filter.starLevel} onChange={(starLevel) => setFilter({ ...filter, starLevel, page: 1 })} options={[1, 2, 3, 4, 5].map((value) => ({ value, label: `${value} 星` }))} style={{ width: 100 }} /><Select allowClear placeholder="难度" value={filter.difficulty} onChange={(difficulty) => setFilter({ ...filter, difficulty, page: 1 })} options={['EASY', 'MEDIUM', 'HARD'].map((value) => ({ value }))} style={{ width: 130 }} /><Select allowClear placeholder="高频度" value={filter.frequencyLevel} onChange={(frequencyLevel) => setFilter({ ...filter, frequencyLevel, page: 1 })} options={['LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH'].map((value) => ({ value }))} style={{ width: 140 }} /><Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditing(undefined); setEditorOpen(true); }}>新增题目</Button></Space></Card>
    <Card title={<Typography.Text strong>题库</Typography.Text>}><Table rowKey="id" columns={columns} dataSource={questions.data.items} pagination={{ current: filter.page, pageSize: filter.pageSize, total: questions.data.total, onChange: (page, pageSize) => setFilter({ ...filter, page, pageSize }) }} /></Card>
    {editorOpen && <QuestionEditor value={editing} onClose={() => setEditorOpen(false)} />}
  </Space>;
}

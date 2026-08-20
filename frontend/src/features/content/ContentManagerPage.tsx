import { DatabaseOutlined, PlusOutlined } from '@ant-design/icons';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, App, Button, Card, Col, Descriptions, Form, Input, InputNumber, Modal, Row, Select, Space, Statistic, Table, Tabs, Tag, Upload } from 'antd';
import { useState } from 'react';
import { ApiRequestError } from '../../api/client';
import { ErrorState, LoadingState } from '../../components/states';
import { PageHeader } from '../../components/ui/PageHeader';
import { contentApi } from './api';

type Editor = 'category' | 'topic' | 'tag' | undefined;
export function ContentManagerPage() {
  const { message } = App.useApp();
  const client = useQueryClient(); const [editor, setEditor] = useState<Editor>(); const [seedOpen, setSeedOpen] = useState(false); const [form] = Form.useForm(); const [seedForm] = Form.useForm();
  const [seedError, setSeedError] = useState<string>();
  const categories = useQuery({ queryKey: ['categories'], queryFn: contentApi.categories }); const topics = useQuery({ queryKey: ['topics'], queryFn: () => contentApi.topics() }); const tags = useQuery({ queryKey: ['tags'], queryFn: contentApi.tags });
  const systemStatus = useQuery({ queryKey: ['v1', 'system-status'], queryFn: contentApi.systemStatus });
  const invalidate = async () => { await client.invalidateQueries({ queryKey: ['categories'] }); await client.invalidateQueries({ queryKey: ['topics'] }); await client.invalidateQueries({ queryKey: ['tags'] }); };
  const save = async () => { const values = await form.validateFields(); if (editor === 'category') await contentApi.createCategory({ ...values, status: values.status ?? 'ENABLED', sortOrder: values.sortOrder ?? 0 }); if (editor === 'topic') await contentApi.createTopic({ ...values, status: values.status ?? 'ENABLED', sortOrder: values.sortOrder ?? 0 }); if (editor === 'tag') await contentApi.createTag(values); await invalidate(); message.success('内容已保存'); setEditor(undefined); form.resetFields(); };
  const importSeed = async () => {
    try {
      setSeedError(undefined);
      const { file } = await seedForm.validateFields();
      const result = await contentApi.importSeed(file[0].originFileObj as File);
      await invalidate();
      message.success(`导入完成：新增 ${result.created}，跳过 ${result.skipped}`);
      setSeedOpen(false);
    } catch (error) {
      setSeedError(error instanceof ApiRequestError ? error.message : '种子文件校验失败，请检查内容后重试。');
    }
  };
  if (categories.isLoading || topics.isLoading || tags.isLoading || systemStatus.isLoading) return <LoadingState />; if (categories.isError || topics.isError || tags.isError || systemStatus.isError || !systemStatus.data) return <ErrorState description="内容管理或系统状态加载失败" />;
  const categoryItems = categories.data ?? [];
  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <PageHeader title="内容管理" description="维护分类、专题、标签和版本化题库种子。" />
    <Card className="section-card" title={<Space><span>系统状态</span><Tag color="green">{systemStatus.data.status}</Tag></Space>}>
      <Row gutter={[12, 12]}>
        <Col xs={12} md={6}><Statistic title="题目" value={systemStatus.data.enabledQuestionCount ?? 0} suffix={`/ ${systemStatus.data.questionCount ?? 0}`} /></Col>
        <Col xs={12} md={6}><Statistic title="场景" value={systemStatus.data.scenarioCount ?? 0} /></Col>
        <Col xs={12} md={6}><Statistic title="源码片段" value={systemStatus.data.sourceSnippetCount ?? 0} /></Col>
        <Col xs={12} md={6}><Statistic title="实验" value={systemStatus.data.labCount ?? 0} /></Col>
      </Row>
      <Descriptions size="small" column={{ xs: 1, md: 3 }} style={{ marginTop: 16 }} items={[
        { key: 'database', label: 'PostgreSQL', children: systemStatus.data.databaseVersion },
        { key: 'flyway', label: 'Flyway', children: `V${systemStatus.data.flywayVersion}` },
        { key: 'checkedAt', label: '检查时间', children: systemStatus.data.checkedAt },
      ]} />
    </Card>
    <Card><Space><Button type="primary" icon={<PlusOutlined />} onClick={() => setEditor('category')}>新增分类</Button><Button icon={<PlusOutlined />} onClick={() => setEditor('topic')}>新增专题</Button><Button icon={<PlusOutlined />} onClick={() => setEditor('tag')}>新增标签</Button><Button icon={<DatabaseOutlined />} onClick={() => setSeedOpen(true)}>导入 JSON 种子</Button></Space></Card>
    <Tabs items={[{ key: 'categories', label: '分类', children: <Table rowKey="id" size="small" dataSource={categories.data} columns={[{ title: '编码', dataIndex: 'code' }, { title: '名称', dataIndex: 'name' }, { title: '状态', dataIndex: 'status' }, { title: '排序', dataIndex: 'sortOrder' }]} /> }, { key: 'topics', label: '专题', children: <Table rowKey="id" size="small" dataSource={topics.data} columns={[{ title: '分类', dataIndex: 'categoryName' }, { title: '编码', dataIndex: 'code' }, { title: '名称', dataIndex: 'name' }, { title: '星级', dataIndex: 'starLevel' }, { title: '状态', dataIndex: 'status' }]} /> }, { key: 'tags', label: '标签', children: <Table rowKey="id" size="small" dataSource={tags.data} columns={[{ title: '编码', dataIndex: 'code' }, { title: '名称', dataIndex: 'name' }]} /> }]} />
    <Modal open={Boolean(editor)} title={editor === 'category' ? '新增分类' : editor === 'topic' ? '新增专题' : '新增标签'} onCancel={() => setEditor(undefined)} onOk={() => void save()} destroyOnHidden>
      <Form form={form} layout="vertical" initialValues={{ status: 'ENABLED', sortOrder: 0, starLevel: 3 }}>
        {editor === 'topic' && <Form.Item name="categoryId" label="所属分类" rules={[{ required: true, message: '请选择分类' }]}><Select options={categoryItems.map((item) => ({ value: item.id, label: item.name }))} /></Form.Item>}
        <Form.Item name="code" label="编码" rules={[{ required: true, message: '请输入编码' }]}><Input placeholder={editor === 'category' ? 'JAVA' : 'topic-java'} /></Form.Item><Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}><Input /></Form.Item>
        {editor !== 'tag' && <><Form.Item name="description" label="说明"><Input.TextArea rows={3} /></Form.Item><Row gutter={16}>{editor === 'topic' && <Col span={12}><Form.Item name="starLevel" label="星级"><InputNumber min={1} max={5} style={{ width: '100%' }} /></Form.Item></Col>}<Col span={12}><Form.Item name="sortOrder" label="排序"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item></Col></Row><Form.Item name="status" label="状态"><Select options={['ENABLED', 'DISABLED'].map((value) => ({ value }))} /></Form.Item></>}
      </Form>
    </Modal>
    <Modal open={seedOpen} title="导入 JSON 题库种子" onCancel={() => { setSeedOpen(false); setSeedError(undefined); }} onOk={() => void importSeed()}><Form form={seedForm} layout="vertical"><Form.Item name="file" label="种子 JSON 文件" valuePropName="fileList" rules={[{ required: true, message: '请选择种子文件' }]}><Upload accept="application/json,.json" maxCount={1} beforeUpload={() => false}><Button>选择 JSON 文件</Button></Upload></Form.Item>{seedError && <Alert type="error" showIcon title="种子导入失败" description={seedError} />}</Form></Modal>
  </Space>;
}

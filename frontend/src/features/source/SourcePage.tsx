import { CodeOutlined, LinkOutlined, SearchOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { Button, Card, Col, Input, Row, Select, Space, Tag, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ErrorState, LoadingState } from '../../components/states';
import { PageHeader } from '../../components/ui/PageHeader';
import { SectionCard } from '../../components/ui/SectionCard';
import { sourceApi } from '../workspaces/api';

export function SourcePage() {
  const navigate = useNavigate();
  const list = useQuery({ queryKey: ['v1', 'source-snippets'], queryFn: sourceApi.list });
  const [library, setLibrary] = useState<string>('ALL');
  const [version, setVersion] = useState<string>('ALL');
  const [keyword, setKeyword] = useState('');
  const [selectedId, setSelectedId] = useState<number>();
  const [selectedLine, setSelectedLine] = useState<number>();

  const filtered = useMemo(() => (list.data ?? []).filter((item) => library === 'ALL' || item.libraryName === library)
    .filter((item) => version === 'ALL' || item.versionLabel === version)
    .filter((item) => !keyword.trim() || `${item.title ?? ''} ${item.summary ?? ''}`.toLocaleLowerCase('zh-CN').includes(keyword.trim().toLocaleLowerCase('zh-CN'))), [keyword, library, list.data, version]);

  useEffect(() => {
    if (filtered[0]?.id && !filtered.some((item) => item.id === selectedId)) setSelectedId(filtered[0].id);
  }, [filtered, selectedId]);

  const detail = useQuery({ queryKey: ['v1', 'source-snippets', selectedId], queryFn: () => sourceApi.detail(selectedId!), enabled: Boolean(selectedId) });

  useEffect(() => {
    if (detail.data) setSelectedLine(detail.data.annotations?.[0]?.lineStart ?? detail.data.startLine ?? 1);
  }, [detail.data]);

  if (list.isLoading) return <LoadingState />;
  if (list.isError || !list.data) return <ErrorState description="源码片段加载失败" />;
  if (detail.isLoading) return <LoadingState />;
  if (detail.isError || !detail.data) return <ErrorState description="源码详情加载失败" />;

  const item = detail.data;
  const startLine = item.startLine ?? 1;
  const lines = (item.codeText ?? '').split('\n');
  const annotation = item.annotations?.find((value) => selectedLine !== undefined && selectedLine >= (value.lineStart ?? 0) && selectedLine <= (value.lineEnd ?? 0));
  const libraries = [...new Set(list.data.map((value) => value.libraryName).filter(Boolean))];
  const versions = [...new Set(list.data.map((value) => value.versionLabel).filter(Boolean))];

  return <Space orientation="vertical" size={20} style={{ width: '100%' }}>
    <PageHeader title="源码 + 注释阅读" description="版本、短教学片段、行内解释与题目关联；不复制第三方完整源码。" />
    <Card className="section-card source-toolbar">
      <Space wrap>
        <Select aria-label="源码库" value={library} onChange={setLibrary} options={[{ value: 'ALL', label: '全部 Library' }, ...libraries.map((value) => ({ value, label: value }))]} />
        <Select aria-label="源码版本" value={version} onChange={setVersion} options={[{ value: 'ALL', label: '全部版本' }, ...versions.map((value) => ({ value, label: value }))]} />
        <Select aria-label="源码文件" value={selectedId} onChange={setSelectedId} options={filtered.map((value) => ({ value: value.id, label: value.title }))} />
        <Input allowClear prefix={<SearchOutlined />} value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="搜索教学片段" />
      </Space>
    </Card>

    <SectionCard>
      <Space orientation="vertical" size={8}>
        <Space wrap><CodeOutlined /><Typography.Title level={3} style={{ margin: 0 }}>{item.title}</Typography.Title><Tag color="blue">{item.language}</Tag><Tag>{item.versionLabel}</Tag></Space>
        <Typography.Text>{item.summary}</Typography.Text>
        <Typography.Text type="secondary">{item.sourcePath} · {item.licenseName}</Typography.Text>
      </Space>
    </SectionCard>

    <Row gutter={[16, 16]} className="source-layout">
      <Col xs={24} lg={16}>
        <div className="source-code" role="list" aria-label="源码行">
          {lines.map((line, index) => {
            const lineNumber = startLine + index;
            const annotated = item.annotations?.some((value) => lineNumber >= (value.lineStart ?? 0) && lineNumber <= (value.lineEnd ?? 0));
            return <button type="button" role="listitem" aria-label={`选择第 ${lineNumber} 行`} className={`source-code-line ${selectedLine === lineNumber ? 'active' : ''} ${annotated ? 'annotated' : ''}`} key={lineNumber} onClick={() => setSelectedLine(lineNumber)}>
              <span className="source-line-number">{String(lineNumber).padStart(2, '0')}</span><code>{line || ' '}</code>
            </button>;
          })}
        </div>
      </Col>
      <Col xs={24} lg={8}>
        <SectionCard title={`Line ${selectedLine ?? startLine} · ${annotation?.title ?? '代码上下文'}`}>
          <Space orientation="vertical" size={14} style={{ width: '100%' }}>
            <Typography.Paragraph>{annotation?.explanation ?? '当前行暂无单独注释，可选择带蓝色标记的代码行。'}</Typography.Paragraph>
            <Typography.Text type="secondary">当前片段共有 {item.annotations?.length ?? 0} 条行级注释。</Typography.Text>
            {item.topicId ? <Button icon={<LinkOutlined />} onClick={() => navigate(`/questions?topicId=${item.topicId}`)}>查看关联题目</Button> : <Button disabled>暂无关联专题</Button>}
          </Space>
        </SectionCard>
      </Col>
    </Row>
  </Space>;
}

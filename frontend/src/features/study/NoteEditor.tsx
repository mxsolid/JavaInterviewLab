import { useEffect, useRef, useState } from 'react';
import { Alert, Button, Input, Space, Typography } from 'antd';
import { ApiRequestError, request } from '../../api/client';

type EditorStatus = 'IDLE' | 'DIRTY' | 'SAVING' | 'SAVED' | 'ERROR';

interface NoteData {
  id: number;
  content: string;
  version: number;
}

interface NoteEditorProps {
  targetType: 'QUESTION' | 'TOPIC';
  targetId: number;
}

async function loadNote(targetType: string, targetId: number) {
  return request<NoteData | null>(`/api/study/notes?targetType=${targetType}&targetId=${targetId}`);
}

/**
 * 可复用笔记编辑器。
 *
 * 保存使用 1000ms 防抖；失败时不以旧服务端值覆盖本地输入，版本冲突只允许用户明确重新加载。
 */
export function NoteEditor({ targetType, targetId }: NoteEditorProps) {
  const [note, setNote] = useState<NoteData | null>(null);
  const [content, setContent] = useState('');
  const [status, setStatus] = useState<EditorStatus>('IDLE');
  const [conflict, setConflict] = useState(false);
  const timerRef = useRef<number | null>(null);

  const reload = async () => {
    if (timerRef.current !== null) window.clearTimeout(timerRef.current);
    const current = await loadNote(targetType, targetId);
    setNote(current);
    setContent(current?.content ?? '');
    setStatus('IDLE');
    setConflict(false);
  };

  useEffect(() => { void reload(); return () => { if (timerRef.current !== null) window.clearTimeout(timerRef.current); }; }, [targetType, targetId]);

  const save = async (nextContent: string, currentNote: NoteData | null) => {
    setStatus('SAVING');
    try {
      const saved = currentNote
        ? await request<NoteData>(`/api/study/notes/${currentNote.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ content: nextContent, version: currentNote.version }) })
        : await request<NoteData>('/api/study/notes', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ targetType, targetId, content: nextContent }) });
      setNote(saved);
      setStatus('SAVED');
      setConflict(false);
    } catch (error) {
      setStatus('ERROR');
      setConflict(error instanceof ApiRequestError && error.message.includes('其他页面'));
    }
  };

  const onChange = (nextContent: string) => {
    setContent(nextContent);
    setStatus('DIRTY');
    setConflict(false);
    if (timerRef.current !== null) window.clearTimeout(timerRef.current);
    const noteSnapshot = note;
    timerRef.current = window.setTimeout(() => { void save(nextContent, noteSnapshot); }, 1000);
  };

  const statusText: Record<EditorStatus, string> = { IDLE: '', DIRTY: '待保存', SAVING: '保存中…', SAVED: '已保存', ERROR: '保存失败，输入内容仍在本地' };
  return <Space direction="vertical" size={8} style={{ width: '100%' }}>
    <Typography.Text type="secondary">学习笔记　{statusText[status]}</Typography.Text>
    {conflict && <Alert type="warning" message="其他页面已修改笔记" action={<Button size="small" onClick={() => void reload()}>重新加载</Button>} />}
    {status === 'ERROR' && !conflict && <Alert type="error" message="笔记保存失败，请稍后继续编辑" />}
    <Input.TextArea value={content} onChange={(event) => onChange(event.target.value)} rows={5} maxLength={20000} placeholder="记录自己的理解、易错点或追问…" />
  </Space>;
}

import { useEffect, useRef, useState } from 'react';
import { Alert, Button, Input, Space, Typography } from 'antd';
import { ApiRequestError } from '../../api/client';
import { studyApi } from './api';
import type { NoteData } from './types';

type EditorStatus = 'IDLE' | 'DIRTY' | 'SAVING' | 'SAVED' | 'ERROR';

interface NoteEditorProps {
  targetType: 'QUESTION' | 'TOPIC';
  targetId: number;
}

/**
 * 同一笔记只允许一个保存请求在途，避免连续输入时第二次请求携带旧 version。
 * 真正跨标签页竞争时后端返回 VERSION_CONFLICT，前端保留当前输入而不覆盖他页内容。
 */
export function NoteEditor({ targetType, targetId }: NoteEditorProps) {
  const [note, setNote] = useState<NoteData | null>(null);
  const [content, setContent] = useState('');
  const [status, setStatus] = useState<EditorStatus>('IDLE');
  const [conflict, setConflict] = useState(false);
  const timerRef = useRef<number | null>(null);
  const serverNoteRef = useRef<NoteData | null>(null);
  const pendingContentRef = useRef<string | null>(null);
  const savingRef = useRef(false);

  const clearTimer = () => {
    if (timerRef.current !== null) {
      window.clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  };

  const reload = async () => {
    clearTimer();
    pendingContentRef.current = null;
    const current = await studyApi.note(targetType, targetId);
    serverNoteRef.current = current;
    setNote(current);
    setContent(current?.content ?? '');
    setStatus('IDLE');
    setConflict(false);
  };

  const flush = async (): Promise<void> => {
    if (savingRef.current || pendingContentRef.current === null) return;
    const contentToSave = pendingContentRef.current;
    if (!serverNoteRef.current && !contentToSave.trim()) {
      pendingContentRef.current = null;
      setStatus('IDLE');
      return;
    }
    pendingContentRef.current = null;
    savingRef.current = true;
    setStatus('SAVING');
    let saved = false;
    try {
      const current = serverNoteRef.current;
      const next = current
        ? await studyApi.updateNote(current.id, contentToSave, current.version)
        : await studyApi.createNote(targetType, targetId, contentToSave);
      serverNoteRef.current = next;
      setNote(next);
      setStatus('SAVED');
      setConflict(false);
      saved = true;
    } catch (error) {
      pendingContentRef.current = contentToSave;
      setStatus('ERROR');
      setConflict(error instanceof ApiRequestError && error.code === 'VERSION_CONFLICT');
    } finally {
      savingRef.current = false;
      // 保存完成后立即处理保存期间的新输入，第二次请求始终使用最新 version。
      if (saved && pendingContentRef.current !== null) void flush();
    }
  };

  const scheduleFlush = () => {
    clearTimer();
    timerRef.current = window.setTimeout(() => { void flush(); }, 1000);
  };

  useEffect(() => {
    void reload();
    return clearTimer;
  }, [targetType, targetId]);

  const onChange = (nextContent: string) => {
    setContent(nextContent);
    pendingContentRef.current = nextContent;
    setStatus('DIRTY');
    setConflict(false);
    scheduleFlush();
  };

  const statusText: Record<EditorStatus, string> = { IDLE: '', DIRTY: '待保存', SAVING: '保存中…', SAVED: '已保存', ERROR: '保存失败，输入内容仍在本地' };
  return <Space orientation="vertical" size={8} style={{ width: '100%' }}>
    <Typography.Text type="secondary">学习笔记　<span role="status" aria-live="polite">{statusText[status]}</span></Typography.Text>
    {conflict && <Alert type="warning" title="其他页面已修改笔记" description="当前输入没有被覆盖。重新加载会读取对方已保存的版本。" action={<Button size="small" onClick={() => void reload()}>重新加载</Button>} />}
    {status === 'ERROR' && !conflict && <Alert type="error" title="笔记保存失败，输入内容仍在本地" action={<Button size="small" onClick={() => void flush()}>重新保存</Button>} />}
    <Input.TextArea aria-label="学习笔记" value={content} onChange={(event) => onChange(event.target.value)} rows={5} maxLength={20000} placeholder="记录自己的理解、易错点或追问…" />
    {note && <Typography.Text type="secondary">最近保存：{new Date(note.updatedAt).toLocaleString('zh-CN')}</Typography.Text>}
  </Space>;
}

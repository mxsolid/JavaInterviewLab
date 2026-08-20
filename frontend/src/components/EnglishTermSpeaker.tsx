import { SoundOutlined } from '@ant-design/icons';
import { Button, Tooltip } from 'antd';

export function EnglishTermSpeaker({ text, speechText }: { text: string; speechText?: string }) {
  const speak = () => {
    if (!('speechSynthesis' in window)) return;
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(speechText ?? text);
    utterance.lang = 'en-US';
    window.speechSynthesis.speak(utterance);
  };
  return <Tooltip title="朗读英文术语"><Button type="text" size="small" icon={<SoundOutlined />} aria-label={`朗读 ${text}`} onClick={speak} /></Tooltip>;
}

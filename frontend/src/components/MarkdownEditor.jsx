import { useState } from 'react';
import MarkdownRenderer from './MarkdownRenderer.jsx';
import { inputCls } from './ui.jsx';

export default function MarkdownEditor({ value, onChange, rows = 8, placeholder = 'Write in Markdown...' }) {
  const [tab, setTab] = useState('write');
  const tabCls = (name) =>
    `px-3 py-1 text-xs font-medium rounded-t border border-b-0 ${
      tab === name ? 'bg-white text-gray-900' : 'bg-gray-100 text-gray-500 hover:text-gray-800'
    }`;

  return (
    <div>
      <div className="flex gap-1">
        <button type="button" className={tabCls('write')} onClick={() => setTab('write')}>
          Write
        </button>
        <button type="button" className={tabCls('preview')} onClick={() => setTab('preview')}>
          Preview
        </button>
      </div>
      {tab === 'write' ? (
        <textarea
          className={`${inputCls} rounded-tl-none font-mono`}
          rows={rows}
          value={value}
          placeholder={placeholder}
          onChange={(e) => onChange(e.target.value)}
        />
      ) : (
        <div className="min-h-[8rem] rounded rounded-tl-none border border-gray-300 bg-white p-3">
          {value.trim() ? (
            <MarkdownRenderer content={value} />
          ) : (
            <p className="text-sm text-gray-400">Nothing to preview yet.</p>
          )}
        </div>
      )}
      <p className="mt-1 text-xs text-gray-400">Markdown + code blocks supported. Use @username to mention someone.</p>
    </div>
  );
}

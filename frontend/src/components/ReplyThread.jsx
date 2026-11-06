import { useState } from 'react';
import toast from 'react-hot-toast';
import api from '../services/api';
import MarkdownEditor from './MarkdownEditor.jsx';
import MarkdownRenderer from './MarkdownRenderer.jsx';
import { errorMessage, formatDate } from '../utils/format';
import { btnPrimary, btnSecondary } from './ui.jsx';

// One reply plus (recursively) all of its children. The backend returns the whole
// tree already nested, so this just renders it.
function ReplyNode({ reply, depth, ctx }) {
  const { discussionId, username, isAdmin, onChanged } = ctx;
  const [replying, setReplying] = useState(false);
  const [editing, setEditing] = useState(false);
  const [text, setText] = useState('');
  const [editText, setEditText] = useState(reply.body);

  const isAuthor = reply.authorUsername === username;

  async function submitReply(e) {
    e.preventDefault();
    try {
      await api.post(`/discussions/${discussionId}/replies`, { body: text, parentReplyId: reply.id });
      setText('');
      setReplying(false);
      onChanged();
    } catch (err) {
      toast.error(errorMessage(err, 'Could not post reply'));
    }
  }

  async function submitEdit(e) {
    e.preventDefault();
    try {
      await api.put(`/replies/${reply.id}`, { body: editText });
      setEditing(false);
      onChanged();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function remove() {
    const n = reply.children.length;
    const warning = n ? ` This will also delete ${n} direct repl${n === 1 ? 'y' : 'ies'} (and anything nested below).` : '';
    if (!window.confirm(`Delete this reply?${warning}`)) return;
    try {
      await api.delete(`/replies/${reply.id}`);
      onChanged();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  // Stop indenting after a few levels so deep threads stay readable on narrow screens.
  const indent = depth > 0 && depth < 6 ? 'ml-4 border-l-2 border-gray-200 pl-4' : depth >= 6 ? 'border-l-2 border-gray-200 pl-2' : '';

  return (
    <div className={indent}>
      <div className="py-2">
        <div className="mb-1 text-xs text-gray-500">
          <span className="font-medium text-gray-700">{reply.authorUsername}</span> · {formatDate(reply.createdAt)}
          {reply.updatedAt !== reply.createdAt && ' · edited'}
        </div>

        {editing ? (
          <form onSubmit={submitEdit} className="space-y-2">
            <MarkdownEditor value={editText} onChange={setEditText} rows={4} />
            <div className="flex gap-2">
              <button className={btnPrimary} type="submit" disabled={!editText.trim()}>
                Save
              </button>
              <button className={btnSecondary} type="button" onClick={() => setEditing(false)}>
                Cancel
              </button>
            </div>
          </form>
        ) : (
          <MarkdownRenderer content={reply.body} />
        )}

        {!editing && (
          <div className="mt-1 flex gap-3 text-xs text-gray-500">
            <button className="hover:text-gray-900" onClick={() => setReplying(!replying)}>
              Reply
            </button>
            {isAuthor && (
              <button className="hover:text-gray-900" onClick={() => setEditing(true)}>
                Edit
              </button>
            )}
            {(isAuthor || isAdmin) && (
              <button className="hover:text-red-600" onClick={remove}>
                Delete
              </button>
            )}
          </div>
        )}

        {replying && (
          <form onSubmit={submitReply} className="mt-2 space-y-2">
            <MarkdownEditor value={text} onChange={setText} rows={3} />
            <div className="flex gap-2">
              <button className={btnPrimary} type="submit" disabled={!text.trim()}>
                Reply
              </button>
              <button className={btnSecondary} type="button" onClick={() => setReplying(false)}>
                Cancel
              </button>
            </div>
          </form>
        )}
      </div>

      {reply.children.map((child) => (
        <ReplyNode key={child.id} reply={child} depth={depth + 1} ctx={ctx} />
      ))}
    </div>
  );
}

export default function ReplyThread({ replies, ...ctx }) {
  return (
    <div className="divide-y divide-gray-100">
      {replies.map((r) => (
        <ReplyNode key={r.id} reply={r} depth={0} ctx={ctx} />
      ))}
    </div>
  );
}

// Handy for "N replies" counters - counts the whole tree, not just the top level.
export function countReplies(replies) {
  return replies.reduce((sum, r) => sum + 1 + countReplies(r.children || []), 0);
}


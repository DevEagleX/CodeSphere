import { useState } from 'react';
import { Link, useNavigate, useOutletContext, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { useAuth } from '../contexts/AuthContext.jsx';
import MarkdownEditor from '../components/MarkdownEditor.jsx';
import MarkdownRenderer from '../components/MarkdownRenderer.jsx';
import ReplyThread, { countReplies } from '../components/ReplyThread.jsx';
import TagBar from '../components/TagBar.jsx';
import { errorMessage, formatDate } from '../utils/format';
import { Card, Empty, ErrorBox, Loading, btnDanger, btnPrimary, btnSecondary, inputCls } from '../components/ui.jsx';

export default function DiscussionPage() {
  const { workspaceId, discussionId } = useParams();
  const { isAdmin } = useOutletContext();
  const { user } = useAuth();
  const navigate = useNavigate();

  const { data: discussion, loading, error, reload } = useFetch(`/discussions/${discussionId}`);
  const replies = useFetch(`/discussions/${discussionId}/replies`);

  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ title: '', body: '' });
  const [newReply, setNewReply] = useState('');

  function startEdit() {
    setForm({ title: discussion.title, body: discussion.body });
    setEditing(true);
  }

  async function saveEdit(e) {
    e.preventDefault();
    try {
      await api.put(`/discussions/${discussionId}`, form);
      setEditing(false);
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function remove() {
    if (!window.confirm('Delete this discussion and all of its replies?')) return;
    try {
      await api.delete(`/discussions/${discussionId}`);
      toast.success('Discussion deleted');
      navigate(`/w/${workspaceId}/channels/${discussion.channelId}`);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function postReply(e) {
    e.preventDefault();
    try {
      await api.post(`/discussions/${discussionId}/replies`, { body: newReply });
      setNewReply('');
      replies.reload(true);
    } catch (err) {
      toast.error(errorMessage(err, 'Could not post reply'));
    }
  }

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const isAuthor = discussion.authorUsername === user?.username;
  const total = replies.data ? countReplies(replies.data) : 0;

  return (
    <div className="space-y-6">
      <Link to={`/w/${workspaceId}/channels/${discussion.channelId}`} className="text-sm text-gray-500 hover:text-gray-900">
        ← Back to channel
      </Link>

      <Card>
        {editing ? (
          <form onSubmit={saveEdit} className="space-y-3">
            <input className={inputCls} value={form.title} required minLength={3} maxLength={200} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            <MarkdownEditor value={form.body} onChange={(body) => setForm({ ...form, body })} />
            <div className="flex gap-2">
              <button className={btnPrimary} type="submit" disabled={!form.body.trim()}>
                Save
              </button>
              <button className={btnSecondary} type="button" onClick={() => setEditing(false)}>
                Cancel
              </button>
            </div>
          </form>
        ) : (
          <>
            <div className="flex items-start justify-between gap-3">
              <h1 className="text-xl font-semibold">{discussion.title}</h1>
              <div className="flex shrink-0 gap-2">
                {isAuthor && (
                  <button className={btnSecondary} onClick={startEdit}>
                    Edit
                  </button>
                )}
                {(isAuthor || isAdmin) && (
                  <button className={btnDanger} onClick={remove}>
                    Delete
                  </button>
                )}
              </div>
            </div>
            <p className="mb-4 mt-1 text-xs text-gray-500">
              {discussion.authorUsername} · {formatDate(discussion.createdAt)}
              {discussion.updatedAt !== discussion.createdAt && ' · edited'}
            </p>
            <MarkdownRenderer content={discussion.body} />
            <div className="mt-4 border-t border-gray-100 pt-3">
              <TagBar workspaceId={workspaceId} type="DISCUSSION" entityId={discussionId} />
            </div>
          </>
        )}
      </Card>

      <section>
        <h2 className="mb-3 text-base font-semibold">
          {total} repl{total === 1 ? 'y' : 'ies'}
        </h2>

        <Card className="mb-4">
          <form onSubmit={postReply} className="space-y-2">
            <MarkdownEditor value={newReply} onChange={setNewReply} rows={4} placeholder="Add a reply..." />
            <button className={btnPrimary} type="submit" disabled={!newReply.trim()}>
              Post reply
            </button>
          </form>
        </Card>

        {replies.loading && <Loading />}
        {replies.error && <ErrorBox message={replies.error} />}
        {replies.data && replies.data.length === 0 && <Empty>No replies yet.</Empty>}
        {replies.data && replies.data.length > 0 && (
          <Card>
            <ReplyThread
              replies={replies.data}
              discussionId={discussionId}
              username={user?.username}
              isAdmin={isAdmin}
              onChanged={() => replies.reload(true)}
            />
          </Card>
        )}
      </section>
    </div>
  );
}

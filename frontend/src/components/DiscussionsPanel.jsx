import { useState } from 'react';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import MarkdownEditor from './MarkdownEditor.jsx';
import { errorMessage, formatDate, snippet } from '../utils/format';
import { Card, Empty, ErrorBox, Loading, Pagination, btnPrimary, btnSecondary, inputCls } from './ui.jsx';

export default function DiscussionsPanel({ workspaceId, channelId }) {
  const url = `/workspaces/${workspaceId}/channels/${channelId}/discussions`;
  const [page, setPage] = useState(0);
  const { data, loading, error, reload } = useFetch(url, { page, size: 10 });

  const [showForm, setShowForm] = useState(false);
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [saving, setSaving] = useState(false);

  async function create(e) {
    e.preventDefault();
    setSaving(true);
    try {
      await api.post(url, { title, body });
      toast.success('Discussion posted');
      setTitle('');
      setBody('');
      setShowForm(false);
      if (page === 0) reload(true);
      else setPage(0);
    } catch (err) {
      toast.error(errorMessage(err, 'Could not post discussion'));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <button className={btnPrimary} onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'New discussion'}
        </button>
      </div>

      {showForm && (
        <Card>
          <form onSubmit={create} className="space-y-3">
            <input className={inputCls} placeholder="Title" value={title} required minLength={3} maxLength={200} onChange={(e) => setTitle(e.target.value)} />
            <MarkdownEditor value={body} onChange={setBody} />
            <div className="flex gap-2">
              <button className={btnPrimary} type="submit" disabled={saving || !body.trim()}>
                {saving ? 'Posting...' : 'Post'}
              </button>
              <button className={btnSecondary} type="button" onClick={() => setShowForm(false)}>
                Cancel
              </button>
            </div>
          </form>
        </Card>
      )}

      {loading && <Loading />}
      {error && <ErrorBox message={error} />}
      {data && data.content.length === 0 && <Empty>No discussions yet - start the first one.</Empty>}

      <div className="space-y-2">
        {(data?.content || []).map((d) => (
          <Link key={d.id} to={`/w/${workspaceId}/discussions/${d.id}`} className="block">
            <Card className="transition-shadow hover:shadow-md">
              <div className="font-medium">{d.title}</div>
              <p className="mt-1 text-sm text-gray-600">{snippet(d.body)}</p>
              <p className="mt-2 text-xs text-gray-400">
                {d.authorUsername} · {formatDate(d.createdAt)}
              </p>
            </Card>
          </Link>
        ))}
      </div>

      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />
    </div>
  );
}

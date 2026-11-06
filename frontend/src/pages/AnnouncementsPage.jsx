import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import MarkdownEditor from '../components/MarkdownEditor.jsx';
import MarkdownRenderer from '../components/MarkdownRenderer.jsx';
import { errorMessage, formatDate } from '../utils/format';
import { Badge, Card, Empty, ErrorBox, Loading, PageHeader, btnDanger, btnPrimary, btnSecondary, inputCls } from '../components/ui.jsx';

export default function AnnouncementsPage() {
  const { workspace, isAdmin } = useOutletContext();
  const wid = workspace.id;
  const { data, loading, error, reload } = useFetch(`/workspaces/${wid}/announcements`);

  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ title: '', body: '', pinned: false });

  async function create(e) {
    e.preventDefault();
    try {
      await api.post(`/workspaces/${wid}/announcements`, form);
      toast.success('Announcement posted');
      setForm({ title: '', body: '', pinned: false });
      setShowForm(false);
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function remove(id) {
    if (!window.confirm('Delete this announcement?')) return;
    try {
      await api.delete(`/workspaces/${wid}/announcements/${id}`);
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  return (
    <div>
      <PageHeader
        title="Announcements"
        subtitle="Broadcasts from workspace admins."
        actions={
          isAdmin && (
            <button className={btnPrimary} onClick={() => setShowForm(!showForm)}>
              {showForm ? 'Cancel' : 'New announcement'}
            </button>
          )
        }
      />

      {showForm && (
        <Card className="mb-4">
          <form onSubmit={create} className="space-y-3">
            <input className={inputCls} placeholder="Title" value={form.title} required minLength={3} maxLength={200} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            <MarkdownEditor value={form.body} onChange={(body) => setForm({ ...form, body })} rows={6} />
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" checked={form.pinned} onChange={(e) => setForm({ ...form, pinned: e.target.checked })} />
              Pin to the top
            </label>
            <div className="flex gap-2">
              <button className={btnPrimary} type="submit" disabled={!form.body.trim()}>
                Post
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
      {data && data.length === 0 && <Empty>No announcements yet.</Empty>}

      <div className="space-y-3">
        {(data || []).map((a) => (
          <Card key={a.id}>
            <div className="flex items-start justify-between gap-3">
              <div className="flex items-center gap-2">
                <h2 className="font-medium">{a.title}</h2>
                {a.pinned && <Badge tone="amber">Pinned</Badge>}
              </div>
              {isAdmin && (
                <button className={btnDanger} onClick={() => remove(a.id)}>
                  Delete
                </button>
              )}
            </div>
            <p className="mb-3 text-xs text-gray-400">
              {a.authorUsername} · {formatDate(a.createdAt)}
            </p>
            <MarkdownRenderer content={a.body} />
          </Card>
        ))}
      </div>
    </div>
  );
}

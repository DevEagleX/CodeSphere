import { useState } from 'react';
import { Link, useNavigate, useOutletContext } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { errorMessage, formatDate, snippet } from '../utils/format';
import { Badge, Card, PageHeader, btnDanger, btnPrimary, btnSecondary, inputCls } from '../components/ui.jsx';

const STATS = [
  ['memberCount', 'Members'],
  ['channelCount', 'Channels'],
  ['discussionCount', 'Discussions'],
  ['replyCount', 'Replies'],
  ['kbArticleCount', 'KB articles'],
  ['documentCount', 'Documents'],
  ['resourceCount', 'Files'],
];

export default function WorkspaceHome() {
  const { workspace, isAdmin, reloadWorkspace } = useOutletContext();
  const navigate = useNavigate();
  const base = `/w/${workspace.id}`;

  const stats = useFetch(`/workspaces/${workspace.id}/analytics`);
  const announcements = useFetch(`/workspaces/${workspace.id}/announcements`);
  const activity = useFetch(`/workspaces/${workspace.id}/activity`, { size: 8 });

  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ name: workspace.name, description: workspace.description || '' });

  async function save(e) {
    e.preventDefault();
    try {
      await api.put(`/workspaces/${workspace.id}`, form);
      toast.success('Workspace updated');
      setEditing(false);
      reloadWorkspace();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function remove() {
    if (!window.confirm(`Delete "${workspace.name}" and everything in it? This cannot be undone.`)) return;
    try {
      await api.delete(`/workspaces/${workspace.id}`);
      toast.success('Workspace deleted');
      navigate('/');
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  return (
    <div className="space-y-8">
      <PageHeader
        title={workspace.name}
        subtitle={workspace.description || 'No description'}
        actions={
          isAdmin && (
            <button className={btnSecondary} onClick={() => setEditing(!editing)}>
              {editing ? 'Cancel' : 'Edit'}
            </button>
          )
        }
      />

      {editing && (
        <Card>
          <form onSubmit={save} className="space-y-3">
            <input className={inputCls} value={form.name} required minLength={2} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            <textarea className={inputCls} rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            <div className="flex gap-2">
              <button className={btnPrimary} type="submit">
                Save
              </button>
              {workspace.currentUserRole === 'OWNER' && (
                <button type="button" className={btnDanger} onClick={remove}>
                  Delete workspace
                </button>
              )}
            </div>
          </form>
        </Card>
      )}

      <section>
        <h2 className="mb-2 text-sm font-semibold uppercase tracking-wide text-gray-500">Analytics</h2>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          {STATS.map(([key, label]) => (
            <Card key={key}>
              <div className="text-2xl font-semibold">{stats.data ? stats.data[key] : '–'}</div>
              <div className="text-xs text-gray-500">{label}</div>
            </Card>
          ))}
        </div>
      </section>

      <section>
        <div className="mb-2 flex items-center justify-between">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-gray-500">Announcements</h2>
          <Link to={`${base}/announcements`} className="text-sm underline">
            View all
          </Link>
        </div>
        <div className="space-y-2">
          {(announcements.data || []).slice(0, 3).map((a) => (
            <Card key={a.id}>
              <div className="flex items-center gap-2">
                <span className="font-medium">{a.title}</span>
                {a.pinned && <Badge tone="amber">Pinned</Badge>}
              </div>
              <p className="mt-1 text-sm text-gray-600">{snippet(a.body, 200)}</p>
            </Card>
          ))}
          {announcements.data && announcements.data.length === 0 && <p className="text-sm text-gray-400">No announcements yet.</p>}
        </div>
      </section>

      <section>
        <div className="mb-2 flex items-center justify-between">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-gray-500">Recent activity</h2>
          <Link to={`${base}/activity`} className="text-sm underline">
            View all
          </Link>
        </div>
        <Card className="divide-y divide-gray-100 !p-0">
          {(activity.data?.content || []).map((ev) => (
            <div key={ev.id} className="flex items-center justify-between gap-3 px-4 py-2 text-sm">
              <span>{ev.description}</span>
              <span className="shrink-0 text-xs text-gray-400">{formatDate(ev.createdAt)}</span>
            </div>
          ))}
          {activity.data && activity.data.content.length === 0 && (
            <p className="px-4 py-3 text-sm text-gray-400">Nothing has happened yet.</p>
          )}
        </Card>
      </section>
    </div>
  );
}

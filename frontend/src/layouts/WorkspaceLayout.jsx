import { useState } from 'react';
import { Link, NavLink, Outlet, useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { errorMessage } from '../utils/format';
import { ErrorBox, Loading, btnPrimary, btnSecondary, inputCls } from '../components/ui.jsx';

const NAV = [
  ['', 'Overview'],
  ['kb', 'Knowledge Base'],
  ['docs', 'Documents'],
  ['resources', 'Files'],
  ['announcements', 'Announcements'],
  ['activity', 'Activity'],
  ['members', 'Members'],
  ['search', 'Search'],
];

const linkCls = ({ isActive }) =>
  `block rounded px-2 py-1 text-sm ${
    isActive ? 'bg-gray-900 text-white' : 'text-gray-700 hover:bg-gray-200'
  }`;

export default function WorkspaceLayout() {
  const { workspaceId } = useParams();
  const navigate = useNavigate();
  const { data: workspace, loading, error, reload: reloadWorkspace } = useFetch(`/workspaces/${workspaceId}`);
  const { data: channels, reload: reloadChannels } = useFetch(`/workspaces/${workspaceId}/channels`);

  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', visibility: 'PUBLIC' });

  async function createChannel(e) {
    e.preventDefault();
    try {
      const res = await api.post(`/workspaces/${workspaceId}/channels`, { ...form, description: '' });
      toast.success(`#${res.data.name} created`);
      setForm({ name: '', visibility: 'PUBLIC' });
      setShowForm(false);
      await reloadChannels(true);
      navigate(`/w/${workspaceId}/channels/${res.data.id}`);
    } catch (err) {
      toast.error(errorMessage(err, 'Could not create channel'));
    }
  }

  if (loading) return <Loading />;
  if (error || !workspace) {
    return (
      <div className="mx-auto mt-12 max-w-xl space-y-3 px-4">
        <ErrorBox message={error || 'Workspace not found'} />
        <Link to="/" className="text-sm underline">
          Back to workspaces
        </Link>
      </div>
    );
  }

  const isAdmin = ['OWNER', 'ADMIN'].includes(workspace.currentUserRole);
  const base = `/w/${workspaceId}`;

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-6 px-4 py-6 md:flex-row">
      <aside className="shrink-0 space-y-6 md:w-56">
        <div>
          <Link to={base} className="block truncate text-base font-semibold">
            {workspace.name}
          </Link>
          <p className="text-xs text-gray-500">Your role: {workspace.currentUserRole}</p>
        </div>

        <nav className="space-y-0.5">
          {NAV.map(([path, label]) => (
            <NavLink key={label} to={path ? `${base}/${path}` : base} end={!path} className={linkCls}>
              {label}
            </NavLink>
          ))}
        </nav>

        <div>
          <div className="mb-1 flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wide text-gray-500">Channels</span>
            <button className="text-lg leading-none text-gray-500 hover:text-gray-900" onClick={() => setShowForm(!showForm)}>
              +
            </button>
          </div>

          {showForm && (
            <form onSubmit={createChannel} className="mb-2 space-y-2 rounded border border-gray-200 bg-white p-2">
              <input
                className={inputCls}
                placeholder="channel-name"
                value={form.name}
                required
                pattern="[a-z0-9\-]+"
                title="Lowercase letters, numbers and hyphens only"
                onChange={(e) => setForm({ ...form, name: e.target.value })}
              />
              <select
                className={inputCls}
                value={form.visibility}
                onChange={(e) => setForm({ ...form, visibility: e.target.value })}
              >
                <option value="PUBLIC">Public</option>
                <option value="PRIVATE">Private</option>
              </select>
              <div className="flex gap-2">
                <button type="submit" className={btnPrimary}>
                  Create
                </button>
                <button type="button" className={btnSecondary} onClick={() => setShowForm(false)}>
                  Cancel
                </button>
              </div>
            </form>
          )}

          <div className="space-y-0.5">
            {(channels || []).map((c) => (
              <NavLink key={c.id} to={`${base}/channels/${c.id}`} className={linkCls}>
                {c.visibility === 'PRIVATE' ? '🔒' : '#'} {c.name}
              </NavLink>
            ))}
            {channels && channels.length === 0 && <p className="px-2 text-xs text-gray-400">No channels yet</p>}
          </div>
        </div>
      </aside>

      <section className="min-w-0 flex-1">
        <Outlet
          context={{
            workspace,
            isAdmin,
            channels: channels || [],
            reloadWorkspace: () => reloadWorkspace(true),
            reloadChannels: () => reloadChannels(true),
          }}
        />
      </section>
    </div>
  );
}

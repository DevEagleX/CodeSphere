import { useState } from 'react';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { useAuth } from '../contexts/AuthContext.jsx';
import { errorMessage, formatDate } from '../utils/format';
import { Badge, Card, ErrorBox, Loading, PageHeader, btnDanger, btnSecondary } from '../components/ui.jsx';

export default function AdminPage() {
  const { user } = useAuth();
  const isPlatformAdmin = user?.role === 'ADMIN';
  const [tab, setTab] = useState('users');
  const users = useFetch(isPlatformAdmin ? '/admin/users' : null);
  const workspaces = useFetch(isPlatformAdmin ? '/admin/workspaces' : null);

  async function setEnabled(u, enabled) {
    try {
      await api.put(`/admin/users/${u.id}/${enabled ? 'enable' : 'disable'}`);
      toast.success(`${u.username} ${enabled ? 'enabled' : 'disabled'}`);
      users.reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  if (!isPlatformAdmin) {
    return (
      <div className="mx-auto max-w-xl px-4 py-12">
        <ErrorBox message="This page is for platform administrators only." />
      </div>
    );
  }

  const tabCls = (name) =>
    `border-b-2 px-3 py-2 text-sm font-medium ${tab === name ? 'border-gray-900 text-gray-900' : 'border-transparent text-gray-500 hover:text-gray-800'}`;

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <PageHeader title="Admin dashboard" subtitle="Platform-wide user and workspace management." />
      <div className="mb-4 flex border-b border-gray-200">
        <button className={tabCls('users')} onClick={() => setTab('users')}>
          Users ({users.data?.length ?? '–'})
        </button>
        <button className={tabCls('workspaces')} onClick={() => setTab('workspaces')}>
          Workspaces ({workspaces.data?.length ?? '–'})
        </button>
      </div>

      {tab === 'users' && (
        <>
          {users.loading && <Loading />}
          {users.error && <ErrorBox message={users.error} />}
          <Card className="divide-y divide-gray-100 !p-0">
            {(users.data || []).map((u) => (
              <div key={u.id} className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
                <div>
                  <span className="font-medium">{u.username}</span>
                  <span className="ml-2 text-sm text-gray-500">{u.email}</span>
                  <div className="text-xs text-gray-400">Joined {formatDate(u.createdAt)}</div>
                </div>
                <div className="flex items-center gap-2">
                  <Badge tone={u.role === 'ADMIN' ? 'amber' : 'gray'}>{u.role}</Badge>
                  <Badge tone={u.enabled ? 'green' : 'red'}>{u.enabled ? 'Active' : 'Disabled'}</Badge>
                  {u.username !== user.username &&
                    (u.enabled ? (
                      <button className={btnDanger} onClick={() => setEnabled(u, false)}>
                        Disable
                      </button>
                    ) : (
                      <button className={btnSecondary} onClick={() => setEnabled(u, true)}>
                        Enable
                      </button>
                    ))}
                </div>
              </div>
            ))}
          </Card>
        </>
      )}

      {tab === 'workspaces' && (
        <>
          {workspaces.loading && <Loading />}
          {workspaces.error && <ErrorBox message={workspaces.error} />}
          <Card className="divide-y divide-gray-100 !p-0">
            {(workspaces.data || []).map((w) => (
              <div key={w.id} className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
                <div>
                  <span className="font-medium">{w.name}</span>
                  <span className="ml-2 text-sm text-gray-500">/{w.slug}</span>
                  <div className="text-xs text-gray-400">
                    Owner {w.ownerUsername} · created {formatDate(w.createdAt)}
                  </div>
                </div>
                <Badge>
                  {w.memberCount} member{w.memberCount === 1 ? '' : 's'}
                </Badge>
              </div>
            ))}
          </Card>
        </>
      )}
    </div>
  );
}

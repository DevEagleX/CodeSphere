import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { useAuth } from '../contexts/AuthContext.jsx';
import { errorMessage, formatDate } from '../utils/format';
import { Badge, Card, ErrorBox, Loading, PageHeader, btnDanger, btnPrimary, inputCls } from '../components/ui.jsx';

export default function MembersPage() {
  const { workspace, isAdmin } = useOutletContext();
  const { user } = useAuth();
  const wid = workspace.id;
  const isOwner = workspace.currentUserRole === 'OWNER';
  const { data: members, loading, error, reload } = useFetch(`/workspaces/${wid}/members`);

  const [form, setForm] = useState({ username: '', role: 'MEMBER' });

  async function addMember(e) {
    e.preventDefault();
    try {
      await api.post(`/workspaces/${wid}/members`, { username: form.username.trim(), role: form.role });
      toast.success(`${form.username.trim()} added`);
      setForm({ username: '', role: 'MEMBER' });
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err, 'Could not add member'));
    }
  }

  async function changeRole(member, role) {
    try {
      await api.put(`/workspaces/${wid}/members/${member.userId}/role`, { role });
      toast.success(`${member.username} is now ${role}`);
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function removeMember(member) {
    if (!window.confirm(`Remove ${member.username} from this workspace?`)) return;
    try {
      await api.delete(`/workspaces/${wid}/members/${member.userId}`);
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  const tone = { OWNER: 'amber', ADMIN: 'blue', MEMBER: 'gray' };

  return (
    <div>
      <PageHeader title="Members" subtitle="Owners and admins can add people and change roles." />

      {isAdmin && (
        <Card className="mb-5">
          <form onSubmit={addMember} className="flex flex-wrap gap-2">
            <input className={`${inputCls} !w-auto min-w-[12rem] flex-1`} placeholder="Username of an existing user" value={form.username} required onChange={(e) => setForm({ ...form, username: e.target.value })} />
            <select className={`${inputCls} !w-auto`} value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
              <option value="MEMBER">Member</option>
              <option value="ADMIN">Admin</option>
            </select>
            <button className={btnPrimary} type="submit">
              Add member
            </button>
          </form>
        </Card>
      )}

      {loading && <Loading />}
      {error && <ErrorBox message={error} />}

      <Card className="divide-y divide-gray-100 !p-0">
        {(members || []).map((m) => {
          const isSelf = m.username === user?.username;
          const canManage = isAdmin && !isSelf && (m.role !== 'OWNER' || isOwner);
          return (
            <div key={m.userId} className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
              <div>
                <span className="font-medium">{m.username}</span>
                {isSelf && <span className="ml-2 text-xs text-gray-400">(you)</span>}
                <div className="text-xs text-gray-400">Joined {formatDate(m.joinedAt)}</div>
              </div>
              <div className="flex items-center gap-2">
                {canManage ? (
                  <select className={`${inputCls} !w-auto !py-1`} value={m.role} onChange={(e) => changeRole(m, e.target.value)}>
                    <option value="MEMBER">Member</option>
                    <option value="ADMIN">Admin</option>
                    {isOwner && <option value="OWNER">Owner</option>}
                  </select>
                ) : (
                  <Badge tone={tone[m.role]}>{m.role}</Badge>
                )}
                {canManage && m.role !== 'OWNER' && (
                  <button className={btnDanger} onClick={() => removeMember(m)}>
                    Remove
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </Card>
    </div>
  );
}

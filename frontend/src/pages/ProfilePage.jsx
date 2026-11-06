import { useState } from 'react';
import toast from 'react-hot-toast';
import api from '../services/api';
import { useAuth } from '../contexts/AuthContext.jsx';
import { errorMessage, formatDate } from '../utils/format';
import { Badge, Card, PageHeader, btnPrimary, inputCls } from '../components/ui.jsx';

export default function ProfilePage() {
  const { user, refreshUser } = useAuth();
  const [form, setForm] = useState({
    displayName: user?.displayName || '',
    bio: user?.bio || '',
    avatarUrl: user?.avatarUrl || '',
  });
  const [saving, setSaving] = useState(false);

  async function save(e) {
    e.preventDefault();
    setSaving(true);
    try {
      await api.put('/users/me/profile', form);
      await refreshUser();
      toast.success('Profile updated');
    } catch (err) {
      toast.error(errorMessage(err, 'Could not update profile'));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="mx-auto max-w-xl px-4 py-8">
      <PageHeader title="Your profile" />
      <Card className="mb-5 flex items-center gap-4">
        {form.avatarUrl ? (
          <img src={form.avatarUrl} alt="" className="h-16 w-16 rounded-full object-cover" onError={(e) => (e.currentTarget.style.display = 'none')} />
        ) : (
          <div className="flex h-16 w-16 items-center justify-center rounded-full bg-gray-200 text-xl font-semibold text-gray-600">
            {user?.username?.[0]?.toUpperCase()}
          </div>
        )}
        <div>
          <div className="font-medium">@{user?.username}</div>
          <div className="text-sm text-gray-500">{user?.email}</div>
          <div className="mt-1 flex items-center gap-2 text-xs text-gray-400">
            <Badge tone={user?.role === 'ADMIN' ? 'amber' : 'gray'}>{user?.role}</Badge>
            Joined {formatDate(user?.createdAt)}
          </div>
        </div>
      </Card>

      <Card>
        <form onSubmit={save} className="space-y-3">
          <div>
            <label className="mb-1 block text-sm font-medium">Display name</label>
            <input className={inputCls} maxLength={100} value={form.displayName} onChange={(e) => setForm({ ...form, displayName: e.target.value })} />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">Bio</label>
            <textarea className={inputCls} rows={3} maxLength={500} value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">Avatar URL</label>
            <input className={inputCls} type="url" placeholder="https://..." value={form.avatarUrl} onChange={(e) => setForm({ ...form, avatarUrl: e.target.value })} />
          </div>
          <button className={btnPrimary} type="submit" disabled={saving}>
            {saving ? 'Saving...' : 'Save profile'}
          </button>
        </form>
      </Card>
    </div>
  );
}

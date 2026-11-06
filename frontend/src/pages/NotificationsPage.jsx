import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { formatDate } from '../utils/format';
import { Badge, Card, Empty, ErrorBox, Loading, PageHeader, Pagination } from '../components/ui.jsx';

export default function NotificationsPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const { data, loading, error, reload } = useFetch('/notifications', { page, size: 20 });

  async function open(n) {
    if (!n.read) {
      try {
        await api.post(`/notifications/${n.id}/read`);
        window.dispatchEvent(new Event('notifications-changed')); // refresh the navbar badge
        reload(true);
      } catch {
        /* non-fatal */
      }
    }
    if (n.link) navigate(n.link);
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <PageHeader title="Notifications" subtitle="Mentions and replies. Click one to jump to it." />
      {loading && <Loading />}
      {error && <ErrorBox message={error} />}
      {data && data.content.length === 0 && <Empty>You're all caught up.</Empty>}

      <div className="space-y-2">
        {(data?.content || []).map((n) => (
          <button key={n.id} onClick={() => open(n)} className="block w-full text-left">
            <Card className={`transition-shadow hover:shadow-md ${n.read ? '' : 'border-blue-200 bg-blue-50'}`}>
              <div className="flex items-center justify-between gap-3">
                <div className="flex items-center gap-2 text-sm">
                  <Badge tone={n.type === 'MENTION' ? 'amber' : 'blue'}>{n.type}</Badge>
                  <span className={n.read ? 'text-gray-600' : 'font-medium'}>{n.message}</span>
                </div>
                <span className="shrink-0 text-xs text-gray-400">{formatDate(n.createdAt)}</span>
              </div>
            </Card>
          </button>
        ))}
      </div>
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />
    </div>
  );
}

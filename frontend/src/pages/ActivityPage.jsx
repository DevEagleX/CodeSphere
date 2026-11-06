import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import useFetch from '../hooks/useFetch';
import { formatDate } from '../utils/format';
import { Card, Empty, ErrorBox, Loading, PageHeader, Pagination } from '../components/ui.jsx';

export default function ActivityPage() {
  const { workspace } = useOutletContext();
  const [page, setPage] = useState(0);
  const { data, loading, error } = useFetch(`/workspaces/${workspace.id}/activity`, { page, size: 20 });

  return (
    <div>
      <PageHeader title="Activity" subtitle="Everything that's happened in this workspace, newest first." />
      {loading && <Loading />}
      {error && <ErrorBox message={error} />}
      {data && data.content.length === 0 && <Empty>No activity yet.</Empty>}
      {data && data.content.length > 0 && (
        <Card className="divide-y divide-gray-100 !p-0">
          {data.content.map((ev) => (
            <div key={ev.id} className="flex items-center justify-between gap-3 px-4 py-3 text-sm">
              <span>{ev.description}</span>
              <span className="shrink-0 text-xs text-gray-400">{formatDate(ev.createdAt)}</span>
            </div>
          ))}
        </Card>
      )}
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />
    </div>
  );
}

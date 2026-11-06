import { useState } from 'react';
import { Link, useOutletContext, useSearchParams } from 'react-router-dom';
import useFetch from '../hooks/useFetch';
import { formatDate } from '../utils/format';
import { Badge, Card, Empty, ErrorBox, Loading, PageHeader, btnPrimary, inputCls } from '../components/ui.jsx';

const TYPES = {
  DISCUSSION: { label: 'Discussion', tone: 'blue', path: 'discussions' },
  KB_ARTICLE: { label: 'KB article', tone: 'green', path: 'kb' },
  DOCUMENT: { label: 'Document', tone: 'amber', path: 'docs' },
};

export default function SearchPage() {
  const { workspace } = useOutletContext();
  const [params, setParams] = useSearchParams();
  const q = params.get('q') || '';
  const [input, setInput] = useState(q);
  const { data, loading, error } = useFetch(q ? `/workspaces/${workspace.id}/search` : null, q ? { q } : undefined);

  function submit(e) {
    e.preventDefault();
    if (input.trim()) setParams({ q: input.trim() });
  }

  return (
    <div>
      <PageHeader title="Search" subtitle="Searches discussions, knowledge base articles and documents." />
      <form onSubmit={submit} className="mb-5 flex gap-2">
        <input className={inputCls} placeholder="Search this workspace..." value={input} onChange={(e) => setInput(e.target.value)} autoFocus />
        <button className={btnPrimary} type="submit">
          Search
        </button>
      </form>

      {loading && <Loading label="Searching..." />}
      {error && <ErrorBox message={error} />}
      {q && data && data.length === 0 && <Empty>No results for “{q}”.</Empty>}

      <div className="space-y-2">
        {(data || []).map((r) => {
          const t = TYPES[r.entityType];
          return (
            <Link key={`${r.entityType}-${r.entityId}`} to={`/w/${workspace.id}/${t.path}/${r.entityId}`} className="block">
              <Card className="transition-shadow hover:shadow-md">
                <div className="flex items-center gap-2">
                  <Badge tone={t.tone}>{t.label}</Badge>
                  <span className="font-medium">{r.title}</span>
                </div>
                <p className="mt-1 text-sm text-gray-600">{r.snippet}</p>
                <p className="mt-1 text-xs text-gray-400">{formatDate(r.createdAt)}</p>
              </Card>
            </Link>
          );
        })}
      </div>
    </div>
  );
}

import { useState } from 'react';
import { Link, useOutletContext, useSearchParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { errorMessage, formatDate } from '../utils/format';
import { Card, Empty, ErrorBox, Loading, PageHeader, btnPrimary, btnSecondary, inputCls } from '../components/ui.jsx';

// Documents form a tree (a document can contain sub-documents, like folders).
// The current folder lives in the URL (?parent=<id>) so it can be bookmarked/shared.
export default function DocumentsPage() {
  const { workspace } = useOutletContext();
  const wid = workspace.id;
  const [searchParams] = useSearchParams();
  const parentId = searchParams.get('parent');

  const docs = useFetch(`/workspaces/${wid}/documents`, parentId ? { parentId } : undefined);
  const parent = useFetch(parentId ? `/documents/${parentId}` : null);

  const [showForm, setShowForm] = useState(false);
  const [title, setTitle] = useState('');

  async function create(e) {
    e.preventDefault();
    try {
      await api.post(`/workspaces/${wid}/documents`, { title, body: '', parentDocumentId: parentId || null });
      toast.success('Document created');
      setTitle('');
      setShowForm(false);
      docs.reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  const upLink = parent.data?.parentDocumentId ? `/w/${wid}/docs?parent=${parent.data.parentDocumentId}` : `/w/${wid}/docs`;

  return (
    <div>
      <PageHeader
        title={parentId ? parent.data?.title || 'Folder' : 'Documents'}
        subtitle={parentId ? undefined : 'Collaborative pages any member can edit, with full version history.'}
        actions={
          <>
            {parentId && (
              <>
                <Link to={upLink} className={btnSecondary}>
                  ← Up
                </Link>
                <Link to={`/w/${wid}/docs/${parentId}`} className={btnSecondary}>
                  Open page
                </Link>
              </>
            )}
            <button className={btnPrimary} onClick={() => setShowForm(!showForm)}>
              {showForm ? 'Cancel' : 'New document'}
            </button>
          </>
        }
      />

      {showForm && (
        <Card className="mb-4">
          <form onSubmit={create} className="flex gap-2">
            <input className={inputCls} placeholder="Document title" value={title} required maxLength={200} onChange={(e) => setTitle(e.target.value)} />
            <button className={btnPrimary} type="submit">
              Create
            </button>
          </form>
        </Card>
      )}

      {docs.loading && <Loading />}
      {docs.error && <ErrorBox message={docs.error} />}
      {docs.data && docs.data.length === 0 && <Empty>{parentId ? 'No sub-documents yet.' : 'No documents yet - create the first one.'}</Empty>}

      <div className="space-y-2">
        {(docs.data || []).map((d) => (
          <Card key={d.id} className="flex items-center justify-between gap-3 transition-shadow hover:shadow-md">
            <Link to={`/w/${wid}/docs/${d.id}`} className="min-w-0 flex-1">
              <div className="truncate font-medium">📄 {d.title}</div>
              <div className="text-xs text-gray-400">
                Last edited by {d.lastEditedByUsername} · {formatDate(d.updatedAt)}
              </div>
            </Link>
            <Link to={`/w/${wid}/docs?parent=${d.id}`} className={btnSecondary}>
              Sub-documents
            </Link>
          </Card>
        ))}
      </div>
    </div>
  );
}

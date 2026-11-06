import { useState } from 'react';
import { Link, useNavigate, useOutletContext, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { useAuth } from '../contexts/AuthContext.jsx';
import MarkdownEditor from '../components/MarkdownEditor.jsx';
import MarkdownRenderer from '../components/MarkdownRenderer.jsx';
import TagBar from '../components/TagBar.jsx';
import { errorMessage, formatDate } from '../utils/format';
import { Card, ErrorBox, Loading, btnDanger, btnPrimary, btnSecondary, inputCls } from '../components/ui.jsx';

export default function DocumentPage() {
  const { workspaceId, documentId } = useParams();
  const { isAdmin } = useOutletContext();
  const { user } = useAuth();
  const navigate = useNavigate();

  const { data: doc, loading, error, reload } = useFetch(`/documents/${documentId}`);
  const [showHistory, setShowHistory] = useState(false);
  const versions = useFetch(showHistory ? `/documents/${documentId}/versions` : null);

  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ title: '', body: '' });

  function startEdit() {
    setForm({ title: doc.title, body: doc.body });
    setEditing(true);
  }

  async function save(e) {
    e.preventDefault();
    try {
      await api.put(`/documents/${documentId}`, form);
      toast.success('Saved - previous version kept in history');
      setEditing(false);
      reload(true);
      if (showHistory) versions.reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function restore(versionNumber) {
    if (!window.confirm(`Restore version ${versionNumber}? The current content is saved to history first, so this is undoable.`)) return;
    try {
      await api.post(`/documents/${documentId}/versions/${versionNumber}/restore`);
      toast.success(`Restored version ${versionNumber}`);
      reload(true);
      versions.reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function remove() {
    if (!window.confirm('Delete this document and all of its sub-documents?')) return;
    try {
      await api.delete(`/documents/${documentId}`);
      toast.success('Document deleted');
      navigate(doc.parentDocumentId ? `/w/${workspaceId}/docs?parent=${doc.parentDocumentId}` : `/w/${workspaceId}/docs`);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const canDelete = isAdmin || doc.createdByUsername === user?.username;
  const backTo = doc.parentDocumentId ? `/w/${workspaceId}/docs?parent=${doc.parentDocumentId}` : `/w/${workspaceId}/docs`;

  return (
    <div className="space-y-4">
      <Link to={backTo} className="text-sm text-gray-500 hover:text-gray-900">
        ← Documents
      </Link>

      <Card>
        {editing ? (
          <form onSubmit={save} className="space-y-3">
            <input className={inputCls} value={form.title} required maxLength={200} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            <MarkdownEditor value={form.body} onChange={(body) => setForm({ ...form, body })} rows={16} />
            <div className="flex gap-2">
              <button className={btnPrimary} type="submit">
                Save
              </button>
              <button className={btnSecondary} type="button" onClick={() => setEditing(false)}>
                Cancel
              </button>
            </div>
          </form>
        ) : (
          <>
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <h1 className="text-xl font-semibold">{doc.title}</h1>
                <p className="mt-1 text-xs text-gray-500">
                  Created by {doc.createdByUsername} · last edited by {doc.lastEditedByUsername} · {formatDate(doc.updatedAt)}
                </p>
              </div>
              <div className="flex shrink-0 flex-wrap gap-2">
                <Link to={`/w/${workspaceId}/docs?parent=${doc.id}`} className={btnSecondary}>
                  Sub-documents
                </Link>
                <button className={btnSecondary} onClick={() => setShowHistory(!showHistory)}>
                  {showHistory ? 'Hide history' : 'History'}
                </button>
                <button className={btnPrimary} onClick={startEdit}>
                  Edit
                </button>
                {canDelete && (
                  <button className={btnDanger} onClick={remove}>
                    Delete
                  </button>
                )}
              </div>
            </div>
            <div className="mt-4">
              {doc.body ? <MarkdownRenderer content={doc.body} /> : <p className="text-sm text-gray-400">This document is empty - click Edit to start writing.</p>}
            </div>
            <div className="mt-4 border-t border-gray-100 pt-3">
              <TagBar workspaceId={workspaceId} type="DOCUMENT" entityId={documentId} />
            </div>
          </>
        )}
      </Card>

      {showHistory && (
        <Card>
          <h2 className="mb-3 text-base font-semibold">Version history</h2>
          {versions.loading && <Loading />}
          {versions.error && <ErrorBox message={versions.error} />}
          {versions.data && versions.data.length === 0 && <p className="text-sm text-gray-400">No earlier versions - history appears after the first edit.</p>}
          <div className="divide-y divide-gray-100">
            {(versions.data || []).map((v) => (
              <details key={v.id} className="py-2">
                <summary className="flex cursor-pointer items-center justify-between gap-3 text-sm">
                  <span>
                    <span className="font-medium">Version {v.versionNumber}</span> · {v.title}
                    <span className="ml-2 text-xs text-gray-400">
                      replaced by {v.editedByUsername} · {formatDate(v.createdAt)}
                    </span>
                  </span>
                  <button
                    type="button"
                    className={btnSecondary}
                    onClick={(e) => {
                      e.preventDefault();
                      restore(v.versionNumber);
                    }}
                  >
                    Restore
                  </button>
                </summary>
                <div className="mt-3 rounded border border-gray-100 bg-gray-50 p-3">
                  {v.body ? <MarkdownRenderer content={v.body} /> : <p className="text-sm text-gray-400">(empty)</p>}
                </div>
              </details>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
}

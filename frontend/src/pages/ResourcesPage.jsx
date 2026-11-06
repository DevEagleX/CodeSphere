import { useRef, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { useAuth } from '../contexts/AuthContext.jsx';
import { errorMessage, formatBytes, formatDate } from '../utils/format';
import { Card, Empty, ErrorBox, Loading, PageHeader, btnDanger, btnPrimary, btnSecondary } from '../components/ui.jsx';

export default function ResourcesPage() {
  const { workspace, isAdmin } = useOutletContext();
  const { user } = useAuth();
  const wid = workspace.id;
  const { data: files, loading, error, reload } = useFetch(`/workspaces/${wid}/resources`);
  const inputRef = useRef(null);
  const [uploading, setUploading] = useState(false);

  async function upload(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    const form = new FormData();
    form.append('file', file);
    setUploading(true);
    try {
      await api.post(`/workspaces/${wid}/resources`, form, { headers: { 'Content-Type': 'multipart/form-data' } });
      toast.success(`${file.name} uploaded`);
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err, 'Upload failed'));
    } finally {
      setUploading(false);
      if (inputRef.current) inputRef.current.value = '';
    }
  }

  // The download endpoint needs the JWT, so a plain <a href> would be rejected.
  // Fetch the bytes through axios (which attaches the token) and save them as a file.
  async function download(file) {
    try {
      const res = await api.get(`/workspaces/${wid}/resources/${file.id}/download`, { responseType: 'blob' });
      const url = URL.createObjectURL(res.data);
      const a = document.createElement('a');
      a.href = url;
      a.download = file.originalFilename;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      toast.error('Download failed');
    }
  }

  async function remove(file) {
    if (!window.confirm(`Delete ${file.originalFilename}?`)) return;
    try {
      await api.delete(`/workspaces/${wid}/resources/${file.id}`);
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  return (
    <div>
      <PageHeader
        title="Files"
        subtitle="PDF, ZIP and image files up to 25 MB."
        actions={
          <>
            <input ref={inputRef} type="file" className="hidden" accept=".pdf,.zip,image/png,image/jpeg,image/gif,image/webp" onChange={upload} />
            <button className={btnPrimary} disabled={uploading} onClick={() => inputRef.current?.click()}>
              {uploading ? 'Uploading...' : 'Upload file'}
            </button>
          </>
        }
      />

      {loading && <Loading />}
      {error && <ErrorBox message={error} />}
      {files && files.length === 0 && <Empty>No files uploaded yet.</Empty>}

      <div className="space-y-2">
        {(files || []).map((f) => (
          <Card key={f.id} className="flex items-center justify-between gap-3">
            <div className="min-w-0">
              <div className="truncate font-medium">{f.originalFilename}</div>
              <div className="text-xs text-gray-400">
                {formatBytes(f.sizeBytes)} · {f.uploadedByUsername} · {formatDate(f.createdAt)}
              </div>
            </div>
            <div className="flex shrink-0 gap-2">
              <button className={btnSecondary} onClick={() => download(f)}>
                Download
              </button>
              {(isAdmin || f.uploadedByUsername === user?.username) && (
                <button className={btnDanger} onClick={() => remove(f)}>
                  Delete
                </button>
              )}
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}

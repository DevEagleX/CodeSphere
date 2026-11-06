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
import { Badge, Card, ErrorBox, Loading, btnDanger, btnPrimary, btnSecondary, inputCls } from '../components/ui.jsx';

export default function ArticlePage() {
  const { workspaceId, articleId } = useParams();
  const { isAdmin } = useOutletContext();
  const { user } = useAuth();
  const navigate = useNavigate();

  const { data: article, loading, error, reload } = useFetch(`/kb/articles/${articleId}`);
  const categories = useFetch(`/workspaces/${workspaceId}/kb/categories`);

  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState(null);

  function startEdit() {
    setForm({ title: article.title, body: article.body, categoryId: article.categoryId || '', published: article.published });
    setEditing(true);
  }

  async function save(e) {
    e.preventDefault();
    try {
      await api.put(`/kb/articles/${articleId}`, { ...form, categoryId: form.categoryId || null });
      toast.success('Article updated');
      setEditing(false);
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function remove() {
    if (!window.confirm('Delete this article?')) return;
    try {
      await api.delete(`/kb/articles/${articleId}`);
      toast.success('Article deleted');
      navigate(`/w/${workspaceId}/kb`);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const isAuthor = article.authorUsername === user?.username;

  return (
    <div className="space-y-4">
      <Link to={`/w/${workspaceId}/kb`} className="text-sm text-gray-500 hover:text-gray-900">
        ← Knowledge Base
      </Link>

      <Card>
        {editing ? (
          <form onSubmit={save} className="space-y-3">
            <input className={inputCls} value={form.title} required minLength={3} maxLength={200} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            <select className={inputCls} value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
              <option value="">Uncategorized</option>
              {(categories.data || []).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
            <MarkdownEditor value={form.body} onChange={(body) => setForm({ ...form, body })} rows={14} />
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" checked={form.published} onChange={(e) => setForm({ ...form, published: e.target.checked })} />
              Published (unpublished articles are hidden from the list)
            </label>
            <div className="flex gap-2">
              <button className={btnPrimary} type="submit" disabled={!form.body.trim()}>
                Save
              </button>
              <button className={btnSecondary} type="button" onClick={() => setEditing(false)}>
                Cancel
              </button>
            </div>
          </form>
        ) : (
          <>
            <div className="flex items-start justify-between gap-3">
              <div>
                <h1 className="text-xl font-semibold">{article.title}</h1>
                <p className="mt-1 flex flex-wrap items-center gap-2 text-xs text-gray-500">
                  {article.authorUsername} · updated {formatDate(article.updatedAt)}
                  {article.categoryName && <Badge>{article.categoryName}</Badge>}
                  {!article.published && <Badge tone="amber">Unpublished</Badge>}
                </p>
              </div>
              <div className="flex shrink-0 gap-2">
                {isAuthor && (
                  <button className={btnSecondary} onClick={startEdit}>
                    Edit
                  </button>
                )}
                {(isAuthor || isAdmin) && (
                  <button className={btnDanger} onClick={remove}>
                    Delete
                  </button>
                )}
              </div>
            </div>
            <div className="mt-4">
              <MarkdownRenderer content={article.body} />
            </div>
            <div className="mt-4 border-t border-gray-100 pt-3">
              <TagBar workspaceId={workspaceId} type="KB_ARTICLE" entityId={articleId} />
            </div>
          </>
        )}
      </Card>
    </div>
  );
}

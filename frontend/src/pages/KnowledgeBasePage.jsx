import { useState } from 'react';
import { Link, useOutletContext } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import MarkdownEditor from '../components/MarkdownEditor.jsx';
import { errorMessage, formatDate, snippet } from '../utils/format';
import { Badge, Card, Empty, ErrorBox, Loading, PageHeader, Pagination, btnPrimary, btnSecondary, inputCls } from '../components/ui.jsx';

export default function KnowledgeBasePage() {
  const { workspace, isAdmin } = useOutletContext();
  const wid = workspace.id;

  const [categoryId, setCategoryId] = useState(null);
  const [page, setPage] = useState(0);
  const categories = useFetch(`/workspaces/${wid}/kb/categories`);
  const articles = useFetch(`/workspaces/${wid}/kb/articles`, { page, size: 10, ...(categoryId ? { categoryId } : {}) });

  const [showArticleForm, setShowArticleForm] = useState(false);
  const [article, setArticle] = useState({ title: '', body: '', categoryId: '' });
  const [newCategory, setNewCategory] = useState('');

  function selectCategory(id) {
    setCategoryId(id);
    setPage(0);
  }

  async function createArticle(e) {
    e.preventDefault();
    try {
      await api.post(`/workspaces/${wid}/kb/articles`, { ...article, categoryId: article.categoryId || null });
      toast.success('Article published');
      setArticle({ title: '', body: '', categoryId: '' });
      setShowArticleForm(false);
      articles.reload(true);
    } catch (err) {
      toast.error(errorMessage(err, 'Could not publish article'));
    }
  }

  async function createCategory(e) {
    e.preventDefault();
    try {
      await api.post(`/workspaces/${wid}/kb/categories`, { name: newCategory });
      setNewCategory('');
      categories.reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function deleteCategory(id) {
    if (!window.confirm('Delete this category? Its articles will become uncategorized.')) return;
    try {
      await api.delete(`/workspaces/${wid}/kb/categories/${id}`);
      if (categoryId === id) selectCategory(null);
      categories.reload(true);
      articles.reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  const catBtn = (active) =>
    `flex w-full items-center justify-between rounded px-2 py-1 text-left text-sm ${active ? 'bg-gray-900 text-white' : 'hover:bg-gray-100'}`;

  return (
    <div>
      <PageHeader
        title="Knowledge Base"
        subtitle="Long-lived documentation for your team."
        actions={
          <button className={btnPrimary} onClick={() => setShowArticleForm(!showArticleForm)}>
            {showArticleForm ? 'Cancel' : 'New article'}
          </button>
        }
      />

      {showArticleForm && (
        <Card className="mb-5">
          <form onSubmit={createArticle} className="space-y-3">
            <input className={inputCls} placeholder="Title" value={article.title} required minLength={3} maxLength={200} onChange={(e) => setArticle({ ...article, title: e.target.value })} />
            <select className={inputCls} value={article.categoryId} onChange={(e) => setArticle({ ...article, categoryId: e.target.value })}>
              <option value="">Uncategorized</option>
              {(categories.data || []).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
            <MarkdownEditor value={article.body} onChange={(body) => setArticle({ ...article, body })} rows={12} />
            <div className="flex gap-2">
              <button className={btnPrimary} type="submit" disabled={!article.body.trim()}>
                Publish
              </button>
              <button className={btnSecondary} type="button" onClick={() => setShowArticleForm(false)}>
                Cancel
              </button>
            </div>
          </form>
        </Card>
      )}

      <div className="flex flex-col gap-6 md:flex-row">
        <aside className="shrink-0 space-y-3 md:w-48">
          <h2 className="text-xs font-semibold uppercase tracking-wide text-gray-500">Categories</h2>
          <button className={catBtn(categoryId === null)} onClick={() => selectCategory(null)}>
            All articles
          </button>
          {(categories.data || []).map((c) => (
            <div key={c.id} className={catBtn(categoryId === c.id)} role="button" onClick={() => selectCategory(c.id)}>
              <span className="truncate">{c.name}</span>
              {isAdmin && (
                <button
                  className="ml-2 text-xs opacity-60 hover:opacity-100"
                  title="Delete category"
                  onClick={(e) => {
                    e.stopPropagation();
                    deleteCategory(c.id);
                  }}
                >
                  ✕
                </button>
              )}
            </div>
          ))}
          <form onSubmit={createCategory} className="flex gap-1">
            <input className={`${inputCls} !py-1`} placeholder="New category" value={newCategory} required minLength={2} maxLength={100} onChange={(e) => setNewCategory(e.target.value)} />
            <button className={btnSecondary} type="submit">
              +
            </button>
          </form>
        </aside>

        <div className="min-w-0 flex-1 space-y-2">
          {articles.loading && <Loading />}
          {articles.error && <ErrorBox message={articles.error} />}
          {articles.data && articles.data.content.length === 0 && <Empty>No articles here yet.</Empty>}
          {(articles.data?.content || []).map((a) => (
            <Link key={a.id} to={`/w/${wid}/kb/${a.id}`} className="block">
              <Card className="transition-shadow hover:shadow-md">
                <div className="flex items-center gap-2">
                  <span className="font-medium">{a.title}</span>
                  {a.categoryName && <Badge>{a.categoryName}</Badge>}
                </div>
                <p className="mt-1 text-sm text-gray-600">{snippet(a.body)}</p>
                <p className="mt-2 text-xs text-gray-400">
                  {a.authorUsername} · {formatDate(a.updatedAt)}
                </p>
              </Card>
            </Link>
          ))}
          <Pagination page={page} totalPages={articles.data?.totalPages} onChange={setPage} />
        </div>
      </div>
    </div>
  );
}

import { useState } from 'react';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { errorMessage } from '../utils/format';
import { Badge, btnSecondary, inputCls } from './ui.jsx';

// Matches the backend TagController's per-type tagging routes.
const PATHS = { DISCUSSION: 'discussions', KB_ARTICLE: 'kb/articles', DOCUMENT: 'documents' };

export default function TagBar({ workspaceId, type, entityId }) {
  const { data: tags, reload } = useFetch(`/workspaces/${workspaceId}/tags/items/${type}/${entityId}`);
  const [name, setName] = useState('');

  async function addTag(e) {
    e.preventDefault();
    if (!name.trim()) return;
    try {
      await api.post(`/workspaces/${workspaceId}/${PATHS[type]}/${entityId}/tags`, { name: name.trim() });
      setName('');
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err, 'Could not add tag'));
    }
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      {(tags || []).map((t) => (
        <Badge key={t.id} tone="blue">
          #{t.name}
        </Badge>
      ))}
      <form onSubmit={addTag} className="flex items-center gap-1">
        <input
          className={`${inputCls} !w-32 !py-1`}
          placeholder="Add tag"
          value={name}
          maxLength={50}
          onChange={(e) => setName(e.target.value)}
        />
        <button className={btnSecondary} type="submit">
          Add
        </button>
      </form>
    </div>
  );
}

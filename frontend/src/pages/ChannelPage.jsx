import { useState } from 'react';
import { useNavigate, useOutletContext, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { useAuth } from '../contexts/AuthContext.jsx';
import DiscussionsPanel from '../components/DiscussionsPanel.jsx';
import PollsPanel from '../components/PollsPanel.jsx';
import { errorMessage } from '../utils/format';
import { Badge, ErrorBox, Loading, PageHeader, btnDanger, btnSecondary, inputCls } from '../components/ui.jsx';

export default function ChannelPage() {
  const { workspaceId, channelId } = useParams();
  const { isAdmin, reloadChannels } = useOutletContext();
  const { user } = useAuth();
  const navigate = useNavigate();
  const { data: channel, loading, error, reload } = useFetch(`/workspaces/${workspaceId}/channels/${channelId}`);

  const [tab, setTab] = useState('discussions');
  const [memberName, setMemberName] = useState('');

  async function addMember(e) {
    e.preventDefault();
    try {
      await api.post(`/workspaces/${workspaceId}/channels/${channelId}/members`, { username: memberName.trim() });
      toast.success(`${memberName.trim()} added to the channel`);
      setMemberName('');
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err, 'Could not add member'));
    }
  }

  async function remove() {
    if (!window.confirm(`Delete #${channel.name} and all its discussions?`)) return;
    try {
      await api.delete(`/workspaces/${workspaceId}/channels/${channelId}`);
      toast.success('Channel deleted');
      await reloadChannels();
      navigate(`/w/${workspaceId}`);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const canDelete = isAdmin || channel.createdByUsername === user?.username;
  const tabCls = (name) =>
    `border-b-2 px-3 py-2 text-sm font-medium ${
      tab === name ? 'border-gray-900 text-gray-900' : 'border-transparent text-gray-500 hover:text-gray-800'
    }`;

  return (
    <div>
      <PageHeader
        title={`${channel.visibility === 'PRIVATE' ? '🔒' : '#'} ${channel.name}`}
        subtitle={channel.description || `Created by ${channel.createdByUsername}`}
        actions={
          <>
            <Badge tone={channel.visibility === 'PRIVATE' ? 'amber' : 'green'}>{channel.visibility}</Badge>
            {canDelete && (
              <button className={btnDanger} onClick={remove}>
                Delete
              </button>
            )}
          </>
        }
      />

      {channel.visibility === 'PRIVATE' && (
        <form onSubmit={addMember} className="mb-4 flex max-w-md gap-2">
          <input className={inputCls} placeholder={`Add a member by username (${channel.memberCount} now)`} value={memberName} onChange={(e) => setMemberName(e.target.value)} required />
          <button className={btnSecondary} type="submit">
            Add
          </button>
        </form>
      )}

      <div className="mb-4 flex border-b border-gray-200">
        <button className={tabCls('discussions')} onClick={() => setTab('discussions')}>
          Discussions
        </button>
        <button className={tabCls('polls')} onClick={() => setTab('polls')}>
          Polls
        </button>
      </div>

      {tab === 'discussions' ? (
        <DiscussionsPanel workspaceId={workspaceId} channelId={channelId} />
      ) : (
        <PollsPanel workspaceId={workspaceId} channelId={channelId} isAdmin={isAdmin} />
      )}
    </div>
  );
}

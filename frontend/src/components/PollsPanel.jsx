import { useState } from 'react';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { useAuth } from '../contexts/AuthContext.jsx';
import { errorMessage, formatDate } from '../utils/format';
import { Badge, Card, Empty, ErrorBox, Loading, btnDanger, btnPrimary, btnSecondary, inputCls } from './ui.jsx';

export default function PollsPanel({ workspaceId, channelId, isAdmin }) {
  const { user } = useAuth();
  const url = `/workspaces/${workspaceId}/channels/${channelId}/polls`;
  const { data: polls, loading, error, reload } = useFetch(url);

  const [showForm, setShowForm] = useState(false);
  const [question, setQuestion] = useState('');
  const [options, setOptions] = useState(['', '']);

  async function create(e) {
    e.preventDefault();
    try {
      await api.post(url, { question, options: options.map((o) => o.trim()).filter(Boolean) });
      toast.success('Poll created');
      setQuestion('');
      setOptions(['', '']);
      setShowForm(false);
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err, 'Could not create poll'));
    }
  }

  async function vote(pollId, optionId) {
    try {
      await api.post(`/polls/${pollId}/options/${optionId}/vote`);
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function close(pollId) {
    try {
      await api.post(`/polls/${pollId}/close`);
      reload(true);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  function setOption(i, value) {
    setOptions(options.map((o, idx) => (idx === i ? value : o)));
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <button className={btnPrimary} onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'New poll'}
        </button>
      </div>

      {showForm && (
        <Card>
          <form onSubmit={create} className="space-y-3">
            <input className={inputCls} placeholder="Question" value={question} required maxLength={300} onChange={(e) => setQuestion(e.target.value)} />
            {options.map((o, i) => (
              <div key={i} className="flex gap-2">
                <input className={inputCls} placeholder={`Option ${i + 1}`} value={o} maxLength={200} onChange={(e) => setOption(i, e.target.value)} />
                {options.length > 2 && (
                  <button type="button" className={btnSecondary} onClick={() => setOptions(options.filter((_, idx) => idx !== i))}>
                    Remove
                  </button>
                )}
              </div>
            ))}
            <div className="flex gap-2">
              {options.length < 10 && (
                <button type="button" className={btnSecondary} onClick={() => setOptions([...options, ''])}>
                  Add option
                </button>
              )}
              <button type="submit" className={btnPrimary} disabled={!question.trim() || options.filter((o) => o.trim()).length < 2}>
                Create poll
              </button>
            </div>
          </form>
        </Card>
      )}

      {loading && <Loading />}
      {error && <ErrorBox message={error} />}
      {polls && polls.length === 0 && <Empty>No polls in this channel yet.</Empty>}

      {(polls || []).map((p) => {
        const hasVoted = Boolean(p.currentUserVotedOptionId);
        const canVote = !hasVoted && !p.closed;
        const canClose = !p.closed && (isAdmin || p.createdByUsername === user?.username);
        return (
          <Card key={p.id}>
            <div className="flex items-start justify-between gap-2">
              <h3 className="font-medium">{p.question}</h3>
              {p.closed && <Badge tone="red">Closed</Badge>}
            </div>
            <div className="mt-3 space-y-2">
              {p.options.map((o) => {
                const pct = p.totalVotes ? Math.round((o.voteCount / p.totalVotes) * 100) : 0;
                const mine = o.id === p.currentUserVotedOptionId;
                return (
                  <button
                    key={o.id}
                    disabled={!canVote}
                    onClick={() => vote(p.id, o.id)}
                    className={`relative w-full overflow-hidden rounded border px-3 py-2 text-left text-sm ${
                      mine ? 'border-gray-900' : 'border-gray-200'
                    } ${canVote ? 'hover:bg-gray-50' : 'cursor-default'}`}
                  >
                    <span className="absolute inset-y-0 left-0 bg-gray-100" style={{ width: `${pct}%` }} />
                    <span className="relative flex justify-between gap-2">
                      <span>
                        {o.text}
                        {mine && ' ✓'}
                      </span>
                      <span className="text-gray-500">
                        {o.voteCount} · {pct}%
                      </span>
                    </span>
                  </button>
                );
              })}
            </div>
            <div className="mt-3 flex items-center justify-between text-xs text-gray-400">
              <span>
                {p.totalVotes} vote{p.totalVotes === 1 ? '' : 's'} · by {p.createdByUsername} · {formatDate(p.createdAt)}
              </span>
              {canClose && (
                <button className={btnDanger} onClick={() => close(p.id)}>
                  Close poll
                </button>
              )}
            </div>
          </Card>
        );
      })}
    </div>
  );
}

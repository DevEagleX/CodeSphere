import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import useFetch from '../hooks/useFetch';
import { errorMessage } from '../utils/format';
import { Badge, Card, Empty, ErrorBox, Loading, PageHeader, btnPrimary, inputCls } from '../components/ui.jsx';

export default function WorkspacesPage() {
  const navigate = useNavigate();
  const { data: workspaces, loading, error } = useFetch('/workspaces');
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm();

  async function onSubmit(values) {
    try {
      const res = await api.post('/workspaces', values);
      toast.success('Workspace created');
      reset();
      navigate(`/w/${res.data.id}`);
    } catch (e) {
      toast.error(errorMessage(e, 'Could not create workspace'));
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <PageHeader title="Your workspaces" subtitle="Each workspace has its own channels, knowledge base, documents and members." />

      {loading && <Loading />}
      {error && <ErrorBox message={error} />}
      {workspaces && workspaces.length === 0 && <Empty>You are not in any workspace yet - create one below.</Empty>}

      <div className="grid gap-3 sm:grid-cols-2">
        {(workspaces || []).map((w) => (
          <Link key={w.id} to={`/w/${w.id}`}>
            <Card className="h-full transition-shadow hover:shadow-md">
              <div className="flex items-start justify-between gap-2">
                <h2 className="font-medium">{w.name}</h2>
                <Badge tone={w.currentUserRole === 'OWNER' ? 'amber' : 'gray'}>{w.currentUserRole}</Badge>
              </div>
              <p className="mt-1 line-clamp-2 text-sm text-gray-500">{w.description || 'No description'}</p>
              <p className="mt-3 text-xs text-gray-400">
                {w.memberCount} member{w.memberCount === 1 ? '' : 's'} · owner {w.ownerUsername}
              </p>
            </Card>
          </Link>
        ))}
      </div>

      <h2 className="mb-3 mt-10 text-base font-semibold">Create a workspace</h2>
      <Card className="max-w-lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
          <div>
            <label className="mb-1 block text-sm font-medium">Name</label>
            <input
              className={inputCls}
              {...register('name', { required: 'Name is required', minLength: { value: 2, message: 'At least 2 characters' } })}
            />
            {errors.name && <p className="mt-1 text-xs text-red-600">{errors.name.message}</p>}
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">Description</label>
            <textarea className={inputCls} rows={2} {...register('description')} />
          </div>
          <button type="submit" disabled={isSubmitting} className={btnPrimary}>
            {isSubmitting ? 'Creating...' : 'Create workspace'}
          </button>
        </form>
      </Card>
    </div>
  );
}

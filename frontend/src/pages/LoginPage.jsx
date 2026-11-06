import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext.jsx';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm();

  async function onSubmit(data) {
    try {
      await login(data.usernameOrEmail, data.password);
      toast.success('Welcome back');
      navigate('/');
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed';
      toast.error(message);
    }
  }

  return (
    <div className="max-w-sm mx-auto mt-16 p-6 bg-white rounded-lg border border-gray-200">
      <h1 className="text-xl font-semibold mb-4">Log in to DevCollab</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1">Username or email</label>
          <input
            className="w-full border border-gray-300 rounded px-3 py-2 text-sm"
            {...register('usernameOrEmail', { required: 'Required' })}
          />
          {errors.usernameOrEmail && (
            <p className="text-red-600 text-xs mt-1">{errors.usernameOrEmail.message}</p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Password</label>
          <input
            type="password"
            className="w-full border border-gray-300 rounded px-3 py-2 text-sm"
            {...register('password', { required: 'Required' })}
          />
          {errors.password && <p className="text-red-600 text-xs mt-1">{errors.password.message}</p>}
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full bg-gray-900 text-white rounded py-2 text-sm font-medium disabled:opacity-50"
        >
          {isSubmitting ? 'Logging in...' : 'Log in'}
        </button>
      </form>

      <p className="text-sm text-gray-500 mt-4">
        Don&apos;t have an account?{' '}
        <Link to="/register" className="text-gray-900 underline">
          Register
        </Link>
      </p>
    </div>
  );
}

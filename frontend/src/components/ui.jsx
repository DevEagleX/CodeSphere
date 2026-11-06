export const btn =
  'inline-flex items-center justify-center rounded px-3 py-1.5 text-sm font-medium transition-colors disabled:opacity-50';
export const btnPrimary = `${btn} bg-gray-900 text-white hover:bg-gray-700`;
export const btnSecondary = `${btn} border border-gray-300 bg-white text-gray-800 hover:bg-gray-100`;
export const btnDanger = `${btn} border border-red-200 bg-white text-red-600 hover:bg-red-50`;
export const inputCls =
  'w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-300';

export function Loading({ label = 'Loading...' }) {
  return <div className="py-10 text-center text-sm text-gray-500">{label}</div>;
}

export function ErrorBox({ message }) {
  return (
    <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{message}</div>
  );
}

export function Empty({ children }) {
  return (
    <div className="rounded border border-dashed border-gray-300 bg-white px-4 py-8 text-center text-sm text-gray-500">
      {children}
    </div>
  );
}

export function Card({ children, className = '' }) {
  return <div className={`rounded-lg border border-gray-200 bg-white p-4 ${className}`}>{children}</div>;
}

export function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="mb-5 flex flex-wrap items-start justify-between gap-3">
      <div className="min-w-0">
        <h1 className="truncate text-xl font-semibold">{title}</h1>
        {subtitle && <p className="mt-0.5 text-sm text-gray-500">{subtitle}</p>}
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  );
}

const TONES = {
  gray: 'bg-gray-100 text-gray-700',
  green: 'bg-green-100 text-green-700',
  amber: 'bg-amber-100 text-amber-700',
  blue: 'bg-blue-100 text-blue-700',
  red: 'bg-red-100 text-red-700',
};

export function Badge({ children, tone = 'gray' }) {
  return (
    <span className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${TONES[tone]}`}>
      {children}
    </span>
  );
}

export function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null;
  return (
    <div className="mt-4 flex items-center justify-center gap-3 text-sm">
      <button className={btnSecondary} disabled={page <= 0} onClick={() => onChange(page - 1)}>
        Previous
      </button>
      <span className="text-gray-500">
        Page {page + 1} of {totalPages}
      </span>
      <button className={btnSecondary} disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>
        Next
      </button>
    </div>
  );
}

import { TrendingUp, TrendingDown } from 'lucide-react'

export function StatCard({ label, value, sub, up }: { label: string; value: string; sub?: string; up?: boolean }) {
  return (
    <div className="card p-4">
      <p className="text-xs text-dim mb-1">{label}</p>
      <p className="text-xl font-semibold text-text font-mono">{value}</p>
      {sub && (
        <p className={`text-xs mt-1 flex items-center gap-1 ${up === undefined ? 'text-dim' : up ? 'text-green' : 'text-red'}`}>
          {up !== undefined && (up ? <TrendingUp size={11} /> : <TrendingDown size={11} />)}{sub}
        </p>
      )}
    </div>
  )
}

export function Empty({ message }: { message: string }) {
  return <div className="py-10 text-center text-dim text-sm">{message}</div>
}

export function Spinner() {
  return (
    <div className="flex justify-center py-12">
      <div className="w-5 h-5 border-2 border-border border-t-green rounded-full animate-spin" />
    </div>
  )
}

export function SectionHeader({ title, action }: { title: string; action?: React.ReactNode }) {
  return (
    <div className="flex items-center justify-between mb-4">
      <h2 className="text-xs font-semibold text-dim uppercase tracking-wider">{title}</h2>
      {action}
    </div>
  )
}

export function StatusBadge({ status }: { status: string }) {
  const map: Record<string, string> = {
    VERIFIED: 'badge-green', COMPLETED: 'badge-green', FILLED: 'badge-green',
    OPEN: 'badge-muted', PENDING: 'badge-muted', PARTIALLY_FILLED: 'badge-muted', MATCHED: 'badge-muted',
    REJECTED: 'badge-red', FAILED: 'badge-red', CANCELLED: 'badge-red', EXPIRED: 'badge-red',
  }
  return <span className={map[status] ?? 'badge-muted'}>{status.replace('_', ' ')}</span>
}

export function SideBadge({ side }: { side: 'BUY' | 'SELL' }) {
  return <span className={side === 'BUY' ? 'badge-green' : 'badge-red'}>{side}</span>
}

export function Price({ value }: { value: number }) {
  return <span className="font-mono">₹{value.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
}

import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Search, Plus } from 'lucide-react'
import { certificateApi, type Certificate } from '../api'
import { Spinner, Empty, StatusBadge } from '../components/ui'
import { useAuth } from '../store/auth'
import { format, parseISO } from 'date-fns'

export default function MarketPage() {
  const [certs, setCerts]     = useState<Certificate[]>([])
  const [search, setSearch]   = useState('')
  const [loading, setLoading] = useState(true)
  const user = useAuth(s => s.user)

  useEffect(() => {
    certificateApi.list()
      .then(r => setCerts(r.data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const filtered = certs.filter(c =>
    c.title.toLowerCase().includes(search.toLowerCase()) ||
    (c.projectLocation ?? '').toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="page-title">Market</h1>
          <p className="text-xs text-dim mt-0.5">Browse and trade government-verified carbon credits</p>
        </div>
        {user && (
          <Link to="/list-certificate" className="btn-ghost text-xs gap-1.5">
            <Plus size={12} /> List Certificate
          </Link>
        )}
      </div>

      <div className="relative">
        <Search size={12} className="absolute left-3 top-1/2 -translate-y-1/2 text-dim" />
        <input className="input pl-8" placeholder="Search by title or location..."
          value={search} onChange={e => setSearch(e.target.value)} />
      </div>

      {loading ? <Spinner /> : filtered.length === 0 ? <Empty message="No certificates found." /> : (
        <div className="card overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="border-b border-border">
                <th className="table-head">Certificate</th>
                <th className="table-head">Registry ID</th>
                <th className="table-head text-right">Available</th>
                <th className="table-head text-right">Total</th>
                <th className="table-head">Status</th>
                <th className="table-head">Listed</th>
                <th className="table-head"></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(c => (
                <tr key={c.id} className="table-row">
                  <td className="table-cell">
                    <p className="font-medium text-text">{c.title}</p>
                    <p className="text-xs text-dim">{c.projectType ?? 'Carbon'} · {c.projectLocation ?? '—'}</p>
                  </td>
                  <td className="table-cell font-mono text-xs text-dim">{c.registryId}</td>
                  <td className="table-cell text-right font-mono text-sm text-green">{c.availableCredits.toLocaleString('en-IN')}</td>
                  <td className="table-cell text-right font-mono text-sm text-dim">{c.totalCredits.toLocaleString('en-IN')}</td>
                  <td className="table-cell"><StatusBadge status={c.status} /></td>
                  <td className="table-cell text-dim text-xs">{format(parseISO(c.listedAt), 'dd MMM yy')}</td>
                  <td className="table-cell text-right">
                    {c.status === 'VERIFIED'
                      ? <Link to={`/trade/${c.id}`} className="btn-ghost text-xs px-2 py-1">Trade →</Link>
                      : <span className="text-xs text-dim">—</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

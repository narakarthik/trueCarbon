import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, TrendingUp, TrendingDown } from 'lucide-react'
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { priceApi, certificateApi, type PricePoint, type Certificate } from '../api'
import { Spinner, StatusBadge } from '../components/ui'
import { format, parseISO } from 'date-fns'

const mock: PricePoint[] = Array.from({ length: 30 }, (_, i) => ({
  recordedOn: new Date(Date.now() - (29 - i) * 86400000).toISOString(),
  openPrice: 1100 + Math.random() * 200,
  highPrice: 1300 + Math.random() * 200,
  lowPrice:  1000 + Math.random() * 150,
  closePrice: 1100 + Math.sin(i / 4) * 180 + Math.random() * 80,
  volume: 500 + Math.random() * 2000,
  tradeCount: Math.floor(5 + Math.random() * 20),
}))

const Tip = ({ active, payload }: any) => {
  if (!active || !payload?.length) return null
  const d = payload[0].payload
  return (
    <div className="card p-2.5 text-xs shadow-lg space-y-1">
      <p className="text-dim">{format(parseISO(d.recordedOn), 'MMM d, yyyy')}</p>
      <p className="text-green font-mono">₹{d.closePrice?.toFixed(2)}</p>
      <p className="text-dim">Vol {d.volume?.toFixed(0)} tCO₂</p>
    </div>
  )
}

export default function HomePage() {
  const [prices, setPrices] = useState<PricePoint[]>(mock)
  const [certs,  setCerts]  = useState<Certificate[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    priceApi.history(30).then(r => setPrices(r.data)).catch(() => {})
    certificateApi.list()
      .then(r => setCerts(r.data.slice(0, 6)))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const last  = prices.at(-1)
  const prev  = prices.at(-2)
  const delta = last && prev ? last.closePrice - prev.closePrice : 0
  const up    = delta >= 0

  return (
    <div className="space-y-6">

      {/* Price header */}
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-xs text-dim uppercase tracking-widest mb-1">Carbon Credit Index · India PAT</p>
          <div className="flex items-end gap-3">
            <span className="text-3xl font-semibold font-mono">₹{last?.closePrice?.toFixed(2) ?? '—'}</span>
            <span className={`text-sm font-mono flex items-center gap-1 mb-1 ${up ? 'text-green' : 'text-red'}`}>
              {up ? <TrendingUp size={13} /> : <TrendingDown size={13} />}
              {up ? '+' : ''}{delta.toFixed(2)} today
            </span>
          </div>
          <p className="text-xs text-dim mt-1">Price per tCO₂ equivalent</p>
        </div>
        <Link to="/market" className="btn-primary text-xs gap-1.5 flex-shrink-0">
          Start Trading <ArrowRight size={12} />
        </Link>
      </div>

      {/* Chart */}
      <div className="card p-4">
        <p className="text-xs text-dim uppercase tracking-wide mb-3">30-Day Price History</p>
        <ResponsiveContainer width="100%" height={180}>
          <AreaChart data={prices} margin={{ top: 4, right: 4, left: -16, bottom: 0 }}>
            <defs>
              <linearGradient id="g1" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%"  stopColor="#22c55e" stopOpacity={0.15} />
                <stop offset="95%" stopColor="#22c55e" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#1e1e2e" vertical={false} />
            <XAxis dataKey="recordedOn" tickFormatter={d => format(parseISO(d), 'MMM d')}
              tick={{ fontSize: 10, fill: '#71717a' }} tickLine={false} axisLine={false} interval={6} />
            <YAxis tick={{ fontSize: 10, fill: '#71717a' }} tickLine={false} axisLine={false}
              tickFormatter={v => `₹${v}`} />
            <Tooltip content={<Tip />} />
            <Area type="monotone" dataKey="closePrice" stroke="#22c55e" strokeWidth={1.5}
              fill="url(#g1)" dot={false}
              activeDot={{ r: 4, fill: '#22c55e', stroke: '#0a0a0f', strokeWidth: 2 }} />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-3">
        {[
          { l: 'Last Price',    v: `₹${last?.closePrice?.toFixed(2) ?? '—'}` },
          { l: '24h Volume',    v: `${last?.volume?.toFixed(0) ?? '—'} tCO₂` },
          { l: 'Trades Today',  v: String(last?.tradeCount ?? '—') },
        ].map(s => (
          <div key={s.l} className="card p-3 text-center">
            <p className="text-xs text-dim mb-1">{s.l}</p>
            <p className="text-sm font-mono font-semibold">{s.v}</p>
          </div>
        ))}
      </div>

      {/* Listings */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <p className="text-xs text-dim uppercase tracking-wide font-medium">Available Certificates</p>
          <Link to="/market" className="text-xs text-green hover:underline">View all →</Link>
        </div>
        {loading ? <Spinner /> : certs.length === 0 ? (
          <div className="card p-6 text-center text-sm text-dim">
            No certificates yet.{' '}
            <Link to="/list-certificate" className="text-green hover:underline">List one →</Link>
          </div>
        ) : (
          <div className="card overflow-hidden">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border">
                  <th className="table-head">Certificate</th>
                  <th className="table-head">Location</th>
                  <th className="table-head text-right">Available (tCO₂)</th>
                  <th className="table-head">Status</th>
                  <th className="table-head"></th>
                </tr>
              </thead>
              <tbody>
                {certs.map(c => (
                  <tr key={c.id} className="table-row">
                    <td className="table-cell">
                      <p className="font-medium text-text">{c.title}</p>
                      <p className="text-xs text-dim">{c.projectType ?? 'Carbon Credit'}</p>
                    </td>
                    <td className="table-cell text-dim text-xs">{c.projectLocation ?? '—'}</td>
                    <td className="table-cell text-right font-mono text-sm">
                      {c.availableCredits.toLocaleString('en-IN')}
                    </td>
                    <td className="table-cell"><StatusBadge status={c.status} /></td>
                    <td className="table-cell text-right">
                      {c.status === 'VERIFIED' && (
                        <Link to={`/trade/${c.id}`} className="text-xs text-green hover:underline">Trade →</Link>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

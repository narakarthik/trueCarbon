import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { tradeHistoryApi, portfolioApi, type Trade, type Holding } from '../api'
import { StatCard, SectionHeader, Spinner, Empty, SideBadge, StatusBadge, Price } from '../components/ui'
import { useAuth } from '../store/auth'
import { format, parseISO } from 'date-fns'

export default function DashboardPage() {
  const user = useAuth(s => s.user)!
  const [trades,   setTrades]   = useState<Trade[]>([])
  const [holdings, setHoldings] = useState<Holding[]>([])
  const [loading,  setLoading]  = useState(true)

  useEffect(() => {
    Promise.all([tradeHistoryApi.myTrades(), portfolioApi.myHoldings()])
      .then(([tr, hr]) => { setTrades(tr.data); setHoldings(hr.data) })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const totalCredits = holdings.reduce((s, h) => s + h.creditsHeld, 0)
  const totalValue   = holdings.reduce((s, h) => s + h.creditsHeld * h.avgBuyPrice, 0)

  if (loading) return <Spinner />

  return (
    <div className="space-y-6">
      <div>
        <h1 className="page-title">Dashboard</h1>
        <p className="text-xs text-dim mt-0.5">Welcome back, {user.name}</p>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <StatCard label="Credits Held"    value={`${totalCredits.toLocaleString('en-IN')} tCO₂`} sub="in portfolio" />
        <StatCard label="Portfolio Value" value={`₹${totalValue.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`} sub="at avg price" />
        <StatCard label="Certificates"    value={String(holdings.length)} sub="holdings" />
        <StatCard label="Trades"          value={String(trades.filter(t => t.status === 'COMPLETED').length)} sub="completed" />
      </div>

      <div className="grid md:grid-cols-2 gap-5">
        <div>
          <SectionHeader title="Holdings" action={
            <Link to="/portfolio" className="text-xs text-green hover:underline">View all →</Link>
          } />
          {holdings.length === 0 ? (
            <div className="card"><Empty message="No holdings yet. Buy some credits." /></div>
          ) : (
            <div className="card overflow-hidden">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="table-head">Certificate</th>
                    <th className="table-head text-right">Credits</th>
                    <th className="table-head text-right">Avg Price</th>
                  </tr>
                </thead>
                <tbody>
                  {holdings.slice(0, 5).map(h => (
                    <tr key={h.id} className="table-row">
                      <td className="table-cell">
                        <p className="text-sm font-medium text-text truncate max-w-[150px]">{h.certificateTitle}</p>
                        {h.creditsReserved > 0 && <p className="text-xs text-dim">{h.creditsReserved} reserved</p>}
                      </td>
                      <td className="table-cell text-right font-mono text-sm">{h.creditsHeld.toLocaleString('en-IN')}</td>
                      <td className="table-cell text-right font-mono text-sm text-dim"><Price value={h.avgBuyPrice} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div>
          <SectionHeader title="Recent Trades" action={
            <Link to="/orders" className="text-xs text-green hover:underline">View all →</Link>
          } />
          {trades.length === 0 ? (
            <div className="card"><Empty message="No trades yet." /></div>
          ) : (
            <div className="card overflow-hidden">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="table-head">Certificate</th>
                    <th className="table-head">Side</th>
                    <th className="table-head text-right">Total</th>
                    <th className="table-head">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {trades.slice(0, 5).map(t => (
                    <tr key={t.id} className="table-row">
                      <td className="table-cell">
                        <p className="text-sm font-medium text-text truncate max-w-[120px]">{t.certificateTitle}</p>
                        <p className="text-xs text-dim">{format(parseISO(t.createdAt), 'dd MMM yy')}</p>
                      </td>
                      <td className="table-cell"><SideBadge side={t.side} /></td>
                      <td className="table-cell text-right font-mono text-sm"><Price value={t.totalAmount} /></td>
                      <td className="table-cell"><StatusBadge status={t.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      <div className="flex gap-3 flex-wrap">
        <Link to="/market" className="btn-primary text-xs">Browse Market</Link>
        <Link to="/list-certificate" className="btn-ghost text-xs">List Certificate</Link>
        <Link to="/orders" className="btn-ghost text-xs">My Orders</Link>
      </div>
    </div>
  )
}

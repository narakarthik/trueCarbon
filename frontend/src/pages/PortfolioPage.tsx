import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { portfolioApi, type Holding } from '../api'
import { Spinner, Empty, Price } from '../components/ui'

export default function PortfolioPage() {
  const [holdings, setHoldings] = useState<Holding[]>([])
  const [loading, setLoading]   = useState(true)

  useEffect(() => {
    portfolioApi.myHoldings()
      .then(r => setHoldings(r.data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const totalValue = holdings.reduce((s, h) => s + h.creditsHeld * h.avgBuyPrice, 0)

  return (
    <div className="space-y-5">
      <h1 className="page-title">Portfolio</h1>

      {holdings.length > 0 && (
        <div className="grid grid-cols-3 gap-3">
          <div className="card p-4">
            <p className="text-xs text-dim mb-1">Total Credits</p>
            <p className="text-xl font-mono font-semibold">
              {holdings.reduce((s, h) => s + h.creditsHeld, 0).toLocaleString('en-IN')} tCO₂
            </p>
          </div>
          <div className="card p-4">
            <p className="text-xs text-dim mb-1">Estimated Value</p>
            <p className="text-xl font-mono font-semibold">
              ₹{totalValue.toLocaleString('en-IN', { maximumFractionDigits: 0 })}
            </p>
          </div>
          <div className="card p-4">
            <p className="text-xs text-dim mb-1">Certificates</p>
            <p className="text-xl font-mono font-semibold">{holdings.length}</p>
          </div>
        </div>
      )}

      {loading ? <Spinner /> : holdings.length === 0 ? (
        <div className="card">
          <div className="py-12 text-center space-y-3">
            <p className="text-dim text-sm">No holdings yet.</p>
            <Link to="/market" className="btn-primary text-xs inline-flex">Browse Market</Link>
          </div>
        </div>
      ) : (
        <div className="card overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="border-b border-border">
                <th className="table-head">Certificate</th>
                <th className="table-head text-right">Held (tCO₂)</th>
                <th className="table-head text-right">Reserved</th>
                <th className="table-head text-right">Available to Sell</th>
                <th className="table-head text-right">Avg Buy Price</th>
                <th className="table-head text-right">Value</th>
                <th className="table-head"></th>
              </tr>
            </thead>
            <tbody>
              {holdings.map(h => (
                <tr key={h.id} className="table-row">
                  <td className="table-cell font-medium text-text">{h.certificateTitle}</td>
                  <td className="table-cell text-right font-mono text-sm">{h.creditsHeld.toLocaleString('en-IN')}</td>
                  <td className="table-cell text-right font-mono text-sm text-dim">
                    {h.creditsReserved > 0 ? h.creditsReserved.toLocaleString('en-IN') : '—'}
                  </td>
                  <td className="table-cell text-right font-mono text-sm text-green">
                    {(h.creditsHeld - h.creditsReserved).toLocaleString('en-IN')}
                  </td>
                  <td className="table-cell text-right font-mono text-sm text-dim"><Price value={h.avgBuyPrice} /></td>
                  <td className="table-cell text-right font-mono text-sm"><Price value={h.creditsHeld * h.avgBuyPrice} /></td>
                  <td className="table-cell text-right">
                    <Link to={`/trade/${h.certificateId}`} className="text-xs text-green hover:underline">Trade →</Link>
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

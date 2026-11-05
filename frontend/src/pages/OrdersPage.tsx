import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { orderApi, tradeHistoryApi, type Order, type Trade } from '../api'
import { Spinner, Empty, SideBadge, StatusBadge, Price } from '../components/ui'
import { format, parseISO } from 'date-fns'

type Tab = 'OPEN' | 'ALL' | 'TRADES'

export default function OrdersPage() {
  const [orders,  setOrders]  = useState<Order[]>([])
  const [trades,  setTrades]  = useState<Trade[]>([])
  const [loading, setLoading] = useState(true)
  const [tab,     setTab]     = useState<Tab>('OPEN')

  const load = () => {
    Promise.all([orderApi.myOrders(), tradeHistoryApi.myTrades()])
      .then(([or, tr]) => { setOrders(or.data); setTrades(tr.data) })
      .catch(() => {})
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const cancel = async (id: string) => {
    try { await orderApi.cancel(id); toast.success('Order cancelled'); load() }
    catch { toast.error('Failed to cancel') }
  }

  const shown = tab === 'OPEN'
    ? orders.filter(o => o.status === 'OPEN' || o.status === 'PARTIALLY_FILLED')
    : orders

  return (
    <div className="space-y-5">
      <h1 className="page-title">Orders & Trades</h1>

      <div className="flex border-b border-border gap-0">
        {(['OPEN', 'ALL', 'TRADES'] as Tab[]).map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-4 py-2 text-xs font-medium border-b-2 transition-colors -mb-px ${
              tab === t ? 'border-green text-green' : 'border-transparent text-dim hover:text-text'
            }`}>
            {t === 'OPEN' ? 'Open Orders' : t === 'ALL' ? 'All Orders' : 'Trade History'}
          </button>
        ))}
      </div>

      {loading ? <Spinner /> : tab === 'TRADES' ? (
        trades.length === 0 ? <Empty message="No trades yet." /> : (
          <div className="card overflow-hidden">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border">
                  <th className="table-head">Certificate</th>
                  <th className="table-head">Side</th>
                  <th className="table-head text-right">Credits</th>
                  <th className="table-head text-right">Price</th>
                  <th className="table-head text-right">Total</th>
                  <th className="table-head">Status</th>
                  <th className="table-head">Date</th>
                </tr>
              </thead>
              <tbody>
                {trades.map(t => (
                  <tr key={t.id} className="table-row">
                    <td className="table-cell font-medium text-text truncate max-w-[160px]">{t.certificateTitle}</td>
                    <td className="table-cell"><SideBadge side={t.side} /></td>
                    <td className="table-cell text-right font-mono text-sm">{t.creditsTraded.toLocaleString('en-IN')}</td>
                    <td className="table-cell text-right font-mono text-sm"><Price value={t.tradePrice} /></td>
                    <td className="table-cell text-right font-mono text-sm"><Price value={t.totalAmount} /></td>
                    <td className="table-cell"><StatusBadge status={t.status} /></td>
                    <td className="table-cell text-dim text-xs">{format(parseISO(t.createdAt), 'dd MMM yy HH:mm')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      ) : (
        shown.length === 0 ? <Empty message={tab === 'OPEN' ? 'No open orders.' : 'No orders yet.'} /> : (
          <div className="card overflow-hidden">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border">
                  <th className="table-head">Certificate</th>
                  <th className="table-head">Side</th>
                  <th className="table-head">Type</th>
                  <th className="table-head text-right">Price Limit</th>
                  <th className="table-head text-right">Credits</th>
                  <th className="table-head text-right">Filled</th>
                  <th className="table-head">Status</th>
                  <th className="table-head">Date</th>
                  <th className="table-head"></th>
                </tr>
              </thead>
              <tbody>
                {shown.map(o => (
                  <tr key={o.id} className="table-row">
                    <td className="table-cell font-medium text-text truncate max-w-[140px]">{o.certificateTitle}</td>
                    <td className="table-cell"><SideBadge side={o.side} /></td>
                    <td className="table-cell text-xs text-dim">{o.orderType}</td>
                    <td className="table-cell text-right font-mono text-sm">
                      {o.orderType === 'INSTANT' ? '—' : <Price value={o.priceLimit} />}
                    </td>
                    <td className="table-cell text-right font-mono text-sm">{o.creditsRequested.toLocaleString('en-IN')}</td>
                    <td className="table-cell text-right font-mono text-sm text-dim">
                      {o.creditsFilled > 0 ? o.creditsFilled.toLocaleString('en-IN') : '—'}
                    </td>
                    <td className="table-cell"><StatusBadge status={o.status} /></td>
                    <td className="table-cell text-dim text-xs">{format(parseISO(o.createdAt), 'dd MMM yy HH:mm')}</td>
                    <td className="table-cell text-right">
                      {(o.status === 'OPEN' || o.status === 'PARTIALLY_FILLED') && (
                        <button onClick={() => cancel(o.id)} className="text-xs text-red hover:underline">Cancel</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}
    </div>
  )
}

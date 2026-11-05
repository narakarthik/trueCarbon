import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { ArrowLeft, RefreshCw } from 'lucide-react'
import { certificateApi, orderApi, type Certificate, type OrderBook } from '../api'
import { useAuth } from '../store/auth'
import { Spinner, Price } from '../components/ui'

const MOCK: OrderBook = {
  bids: [
    { price: 1350, credits: 200, count: 2 },
    { price: 1300, credits: 150, count: 1 },
    { price: 1250, credits: 500, count: 3 },
    { price: 1200, credits: 80,  count: 1 },
    { price: 1150, credits: 320, count: 2 },
  ],
  asks: [
    { price: 1400, credits: 250, count: 2 },
    { price: 1450, credits: 100, count: 1 },
    { price: 1500, credits: 300, count: 3 },
    { price: 1550, credits: 180, count: 1 },
    { price: 1600, credits: 420, count: 2 },
  ],
  lastPrice: 1375,
}

export default function TradePage() {
  const { certId } = useParams<{ certId: string }>()
  const navigate   = useNavigate()
  const user       = useAuth(s => s.user)

  const [cert,    setCert]    = useState<Certificate | null>(null)
  const [book,    setBook]    = useState<OrderBook>(MOCK)
  const [loading, setLoading] = useState(true)
  const [side,    setSide]    = useState<'BUY' | 'SELL'>('BUY')
  const [type,    setType]    = useState<'LIMIT' | 'INSTANT'>('LIMIT')
  const [price,   setPrice]   = useState('')
  const [credits, setCredits] = useState('')
  const [placing, setPlacing] = useState(false)

  const total = price && credits ? parseFloat(price) * parseFloat(credits) : 0

  const loadBook = () => {
    if (certId) orderApi.orderBook(certId).then(r => setBook(r.data)).catch(() => {})
  }

  useEffect(() => {
    if (!certId) return
    Promise.all([certificateApi.get(certId), orderApi.orderBook(certId)])
      .then(([cr, br]) => { setCert(cr.data); setBook(br.data) })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [certId])

  const placeOrder = async () => {
    if (!user) { navigate('/login'); return }
    if (!credits || parseFloat(credits) <= 0) { toast.error('Enter valid credits'); return }
    if (type === 'LIMIT' && (!price || parseFloat(price) <= 0)) { toast.error('Enter valid price'); return }
    setPlacing(true)
    try {
      if (type === 'INSTANT') {
        await orderApi.placeInstantBuy({ certificateId: certId!, credits: parseFloat(credits) })
        toast.success('Instant buy placed!')
      } else {
        await orderApi.placeLimitOrder({ certificateId: certId!, side, priceLimit: parseFloat(price), credits: parseFloat(credits) })
        toast.success(`${side} limit order placed!`)
      }
      setPrice(''); setCredits(''); loadBook()
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? 'Failed to place order')
    } finally { setPlacing(false) }
  }

  if (loading) return <Spinner />

  const spread = book.asks[0] && book.bids[0] ? (book.asks[0].price - book.bids[0].price).toFixed(2) : '—'

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate(-1)} className="text-dim hover:text-text">
          <ArrowLeft size={16} />
        </button>
        <div className="flex-1 min-w-0">
          <h1 className="page-title truncate">{cert?.title ?? 'Certificate'}</h1>
          <p className="text-xs text-dim mt-0.5">{cert?.projectType} · {cert?.projectLocation} · ID: {cert?.registryId}</p>
        </div>
        <div className="text-right flex-shrink-0">
          <p className="text-xl font-mono font-semibold">₹{book.lastPrice.toLocaleString('en-IN')}</p>
          <p className="text-xs text-dim">Last price</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">

        {/* Order book */}
        <div className="lg:col-span-2 space-y-3">
          <div className="flex items-center justify-between">
            <p className="text-xs text-dim uppercase tracking-wide font-medium">Order Book</p>
            <button onClick={loadBook} className="text-dim hover:text-text"><RefreshCw size={12} /></button>
          </div>

          <div className="card overflow-hidden">
            {/* Header */}
            <div className="grid grid-cols-3 px-4 py-2 border-b border-border bg-bg/40 text-xs text-dim">
              <span>Price (₹)</span><span className="text-center">Credits (tCO₂)</span><span className="text-right">Orders</span>
            </div>

            {/* Asks — reversed so cheapest is nearest spread */}
            {[...book.asks].reverse().map((a, i) => {
              const pct = (a.credits / Math.max(...book.asks.map(x => x.credits))) * 100
              return (
                <div key={i} className="relative grid grid-cols-3 px-4 py-1.5 hover:bg-white/[0.015]">
                  <div className="absolute inset-y-0 right-0 bg-red/[0.07]" style={{ width: `${pct}%` }} />
                  <span className="font-mono text-sm text-red relative">{a.price.toLocaleString('en-IN')}</span>
                  <span className="font-mono text-sm text-center relative">{a.credits.toLocaleString('en-IN')}</span>
                  <span className="text-xs text-dim text-right relative">{a.count}</span>
                </div>
              )
            })}

            {/* Spread / last price */}
            <div className="border-y border-border px-4 py-2 flex items-center justify-between bg-bg/60">
              <span className="text-xs text-dim">Spread ₹{spread}</span>
              <span className="font-mono text-base font-semibold text-green">₹{book.lastPrice.toLocaleString('en-IN')}</span>
              <span className="text-xs text-dim">Last traded</span>
            </div>

            {/* Bids */}
            {book.bids.map((b, i) => {
              const pct = (b.credits / Math.max(...book.bids.map(x => x.credits))) * 100
              return (
                <div key={i} className="relative grid grid-cols-3 px-4 py-1.5 hover:bg-white/[0.015]">
                  <div className="absolute inset-y-0 right-0 bg-green/[0.07]" style={{ width: `${pct}%` }} />
                  <span className="font-mono text-sm text-green relative">{b.price.toLocaleString('en-IN')}</span>
                  <span className="font-mono text-sm text-center relative">{b.credits.toLocaleString('en-IN')}</span>
                  <span className="text-xs text-dim text-right relative">{b.count}</span>
                </div>
              )
            })}
          </div>

          {/* Cert info */}
          {cert && (
            <div className="card p-4">
              <p className="text-xs text-dim uppercase tracking-wide mb-3">Certificate Details</p>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                {[
                  { l: 'Total Credits', v: `${cert.totalCredits.toLocaleString('en-IN')} tCO₂` },
                  { l: 'Available',     v: `${cert.availableCredits.toLocaleString('en-IN')} tCO₂` },
                  { l: 'Vintage Year',  v: String(cert.vintageYear ?? '—') },
                  { l: 'Registry ID',   v: cert.registryId },
                ].map(s => (
                  <div key={s.l}>
                    <p className="text-xs text-dim mb-0.5">{s.l}</p>
                    <p className="text-sm font-mono truncate">{s.v}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Order form */}
        <div className="card p-4 h-fit space-y-4">
          <p className="text-xs text-dim uppercase tracking-wide font-medium">Place Order</p>

          {!user ? (
            <div className="text-center py-4">
              <p className="text-sm text-dim mb-3">Sign in to trade</p>
              <a href="/login" className="btn-primary text-xs w-full justify-center">Sign In</a>
            </div>
          ) : (
            <>
              {/* Order type */}
              <div className="flex border border-border rounded overflow-hidden text-xs">
                {(['LIMIT', 'INSTANT'] as const).map(t => (
                  <button key={t} onClick={() => setType(t)}
                    className={`flex-1 py-1.5 font-medium transition-colors ${type === t ? 'bg-surface text-text' : 'text-dim hover:text-text'}`}>
                    {t === 'INSTANT' ? 'Instant Buy' : 'Limit'}
                  </button>
                ))}
              </div>

              {/* Buy / Sell — limit only */}
              {type === 'LIMIT' && (
                <div className="flex border border-border rounded overflow-hidden text-xs">
                  <button onClick={() => setSide('BUY')}
                    className={`flex-1 py-1.5 font-medium transition-colors ${side === 'BUY' ? 'bg-green text-black' : 'text-dim hover:text-green'}`}>
                    Buy
                  </button>
                  <button onClick={() => setSide('SELL')}
                    className={`flex-1 py-1.5 font-medium transition-colors ${side === 'SELL' ? 'bg-red text-white' : 'text-dim hover:text-red'}`}>
                    Sell
                  </button>
                </div>
              )}

              {/* Price */}
              {type === 'LIMIT' && (
                <div>
                  <label className="label">{side === 'BUY' ? 'Max Price' : 'Min Price'} (₹/tCO₂)</label>
                  <input className="input font-mono" type="number" placeholder="0.00"
                    value={price} onChange={e => setPrice(e.target.value)} min="0" step="0.01" />
                </div>
              )}

              {/* Credits */}
              <div>
                <label className="label">Credits (tCO₂)</label>
                <input className="input font-mono" type="number" placeholder="0.0000"
                  value={credits} onChange={e => setCredits(e.target.value)} min="0" step="0.0001" />
              </div>

              {/* Total */}
              {total > 0 && (
                <div className="flex justify-between text-sm border-t border-border pt-3">
                  <span className="text-dim">Total</span>
                  <span className="font-mono font-semibold"><Price value={total} /></span>
                </div>
              )}

              <button onClick={placeOrder} disabled={placing}
                className={`w-full justify-center btn text-sm font-medium ${
                  type === 'INSTANT' || side === 'BUY'
                    ? 'bg-green text-black hover:bg-green-dim'
                    : 'bg-red/80 text-white hover:bg-red'
                }`}>
                {placing ? 'Placing...' : type === 'INSTANT' ? 'Buy Now' : `Place ${side}`}
              </button>

              <p className="text-xs text-dim text-center">
                {type === 'LIMIT' ? 'Auto-executes when price matches' : 'Fills at best available ask'}
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

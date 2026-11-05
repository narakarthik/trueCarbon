import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Leaf } from 'lucide-react'
import toast from 'react-hot-toast'
import { authApi } from '../api'
import { useAuth } from '../store/auth'

export default function RegisterPage() {
  const [name, setName]         = useState('')
  const [email, setEmail]       = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading]   = useState(false)
  const { setAuth } = useAuth()
  const navigate = useNavigate()

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (password.length < 8) { toast.error('Password must be 8+ characters'); return }
    setLoading(true)
    try {
      const res = await authApi.register({ name, email, password })
      setAuth(res.data.user, res.data.accessToken)
      toast.success('Account created!')
      navigate('/dashboard')
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? 'Registration failed')
    } finally { setLoading(false) }
  }

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <div className="w-full max-w-sm">
        <div className="text-center mb-6">
          <div className="inline-flex items-center gap-2 mb-3">
            <Leaf size={16} className="text-green" />
            <span className="font-semibold">TrueCarbon</span>
          </div>
          <h1 className="text-lg font-semibold">Create account</h1>
          <p className="text-sm text-dim mt-1">Start trading carbon credits</p>
        </div>
        <div className="card p-6">
          <form onSubmit={submit} className="space-y-4">
            <div>
              <label className="label">Full Name</label>
              <input className="input" placeholder="Your name"
                value={name} onChange={e => setName(e.target.value)} required />
            </div>
            <div>
              <label className="label">Email</label>
              <input className="input" type="email" placeholder="you@company.com"
                value={email} onChange={e => setEmail(e.target.value)} required />
            </div>
            <div>
              <label className="label">Password</label>
              <input className="input" type="password" placeholder="Min 8 characters"
                value={password} onChange={e => setPassword(e.target.value)} required />
            </div>
            <button className="btn-primary w-full justify-center" disabled={loading}>
              {loading ? 'Creating...' : 'Create Account'}
            </button>
          </form>
          <p className="text-center text-xs text-dim mt-4">
            Have an account? <Link to="/login" className="text-green hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}

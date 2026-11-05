import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { certificateApi } from '../api'

const TYPES = ['Reforestation','Mangrove','Solar Energy','Wind Energy','Methane Capture','Industrial Efficiency','Agriculture','Other']

export default function ListCertPage() {
  const navigate  = useNavigate()
  const [loading, setLoading] = useState(false)
  const [file,    setFile]    = useState<File | null>(null)
  const [form, setForm] = useState({
    title: '', description: '', totalCredits: '',
    registryId: '', projectType: '', projectLocation: '', vintageYear: '',
  })
  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    setForm(f => ({ ...f, [k]: e.target.value }))

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.title || !form.totalCredits || !form.registryId) { toast.error('Fill required fields'); return }
    setLoading(true)
    try {
      const fd = new FormData()
      fd.append('data', new Blob([JSON.stringify({
        ...form,
        totalCredits: parseFloat(form.totalCredits),
        vintageYear: form.vintageYear ? parseInt(form.vintageYear) : undefined,
      })], { type: 'application/json' }))
      if (file) fd.append('certFile', file)
      await certificateApi.create(fd)
      toast.success('Certificate submitted for verification')
      navigate('/market')
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? 'Submission failed')
    } finally { setLoading(false) }
  }

  return (
    <div className="max-w-xl mx-auto space-y-5">
      <h1 className="page-title">List a Certificate</h1>

      <div className="card p-3 text-xs text-dim border-l-2 border-green/40 rounded-l-none">
        Certificate will be marked <strong className="text-green">PENDING</strong> until the Registry ID
        is verified against MoEFCC / BEE / CPCB records.
      </div>

      <form onSubmit={submit} className="card p-5 space-y-4">
        <div>
          <label className="label">Title *</label>
          <input className="input" placeholder="e.g. Rajasthan Solar Farm – Block A"
            value={form.title} onChange={set('title')} required />
        </div>

        <div>
          <label className="label">Description</label>
          <textarea className="input resize-none" rows={3} placeholder="Describe the project"
            value={form.description} onChange={set('description')} />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Total Credits (tCO₂) *</label>
            <input className="input font-mono" type="number" placeholder="500" step="0.0001"
              value={form.totalCredits} onChange={set('totalCredits')} required />
          </div>
          <div>
            <label className="label">Vintage Year</label>
            <input className="input font-mono" type="number" placeholder="2024" min="2000" max="2030"
              value={form.vintageYear} onChange={set('vintageYear')} />
          </div>
        </div>

        <div>
          <label className="label">Government Registry ID *</label>
          <input className="input font-mono" placeholder="e.g. CPCB-CC-2024-001234"
            value={form.registryId} onChange={set('registryId')} required />
          <p className="text-xs text-dim mt-1">Unique ID from MoEFCC, BEE or CPCB</p>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Project Type</label>
            <select className="input" value={form.projectType} onChange={set('projectType')}>
              <option value="">Select type</option>
              {TYPES.map(t => <option key={t}>{t}</option>)}
            </select>
          </div>
          <div>
            <label className="label">Location</label>
            <input className="input" placeholder="e.g. Rajasthan, India"
              value={form.projectLocation} onChange={set('projectLocation')} />
          </div>
        </div>

        <div>
          <label className="label">Certificate PDF (optional)</label>
          <label className={`flex items-center gap-2 border border-dashed rounded-md px-3 py-2.5 cursor-pointer text-sm transition-colors ${
            file ? 'border-green/40 text-green' : 'border-border text-dim hover:border-muted'
          }`}>
            <input type="file" accept=".pdf" className="hidden"
              onChange={e => setFile(e.target.files?.[0] ?? null)} />
            {file ? file.name : 'Click to upload PDF'}
          </label>
        </div>

        <div className="flex gap-3 pt-1">
          <button type="button" onClick={() => navigate(-1)} className="btn-ghost flex-1 justify-center">
            Cancel
          </button>
          <button type="submit" disabled={loading} className="btn-primary flex-1 justify-center">
            {loading ? 'Submitting...' : 'Submit Certificate'}
          </button>
        </div>
      </form>
    </div>
  )
}

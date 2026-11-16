import { useEffect, useMemo, useState } from 'react'
import { Download, FileText, ImagePlus, Link2, Trash2, Upload, X } from 'lucide-react'

import Navbar from '../components/Navbar.jsx'
import {
  deleteExam,
  downloadExam,
  getMyAppointments,
  getMyExams,
  shareExam,
  unshareExam,
  uploadExam,
} from '../services/examApi.js'
import './meus-exames.css'

export default function MeusExames({ user, onLogout, onNavigate }) {
  const [exams, setExams] = useState([])
  const [appointments, setAppointments] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [isUploading, setIsUploading] = useState(false)
  const [busyExamId, setBusyExamId] = useState(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [form, setForm] = useState({ title: '', examDate: '', laboratory: '', file: null })

  useEffect(() => {
    const controller = new AbortController()
    async function loadData() {
      try {
        const [examData, appointmentData] = await Promise.all([
          getMyExams(controller.signal),
          getMyAppointments(controller.signal),
        ])
        setExams(examData)
        setAppointments(appointmentData)
      } catch (loadError) {
        if (loadError.name !== 'AbortError') setError(loadError.message)
      } finally {
        if (!controller.signal.aborted) setIsLoading(false)
      }
    }
    loadData()
    return () => controller.abort()
  }, [])

  const shareableAppointments = useMemo(() => appointments
    .filter((appointment) => ['SCHEDULED', 'COMPLETED'].includes(appointment.status))
    .sort((first, second) => new Date(second.startAt) - new Date(first.startAt)), [appointments])

  function updateField(event) {
    const { name, value, files } = event.target
    setForm((current) => ({ ...current, [name]: files ? files[0] || null : value }))
  }

  async function handleUpload(event) {
    event.preventDefault()
    setError('')
    setSuccess('')
    if (!form.file) {
      setError('Escolha uma foto ou arquivo de exame.')
      return
    }
    setIsUploading(true)
    try {
      const exam = await uploadExam(form)
      setExams((current) => [exam, ...current])
      setForm({ title: '', examDate: '', laboratory: '', file: null })
      event.target.reset()
      setSuccess('Exame adicionado à sua biblioteca.')
    } catch (uploadError) {
      setError(uploadError.message)
    } finally {
      setIsUploading(false)
    }
  }

  async function handleDownload(exam) {
    setError('')
    setBusyExamId(exam.id)
    try {
      await downloadExam(exam)
    } catch (downloadError) {
      setError(downloadError.message)
    } finally {
      setBusyExamId(null)
    }
  }

  async function handleDelete(exam) {
    if (!window.confirm(`Excluir "${exam.title}"?`)) return
    setError('')
    setBusyExamId(exam.id)
    try {
      await deleteExam(exam.id)
      setExams((current) => current.filter((item) => item.id !== exam.id))
      setSuccess('Exame excluído.')
    } catch (deleteError) {
      setError(deleteError.message)
    } finally {
      setBusyExamId(null)
    }
  }

  async function handleShare(exam, appointmentId) {
    if (!appointmentId) return
    setError('')
    setBusyExamId(exam.id)
    try {
      const updated = await shareExam(exam.id, appointmentId)
      setExams((current) => current.map((item) => item.id === exam.id ? updated : item))
      setSuccess('Exame compartilhado com o médico da consulta.')
    } catch (shareError) {
      setError(shareError.message)
    } finally {
      setBusyExamId(null)
    }
  }

  async function handleUnshare(exam, appointmentId) {
    setError('')
    setBusyExamId(exam.id)
    try {
      await unshareExam(exam.id, appointmentId)
      setExams((current) => current.map((item) => item.id === exam.id
        ? { ...item, sharedAppointmentIds: item.sharedAppointmentIds.filter((id) => id !== appointmentId) }
        : item))
      setSuccess('Compartilhamento removido.')
    } catch (shareError) {
      setError(shareError.message)
    } finally {
      setBusyExamId(null)
    }
  }

  function formatDate(value) {
    return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' }).format(new Date(`${value}T12:00:00`))
  }

  function formatAppointment(appointment) {
    const date = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' }).format(new Date(appointment.startAt))
    return `${date} · ${appointment.doctorName}`
  }

  return (
    <div className="patient-app exams-page">
      <Navbar user={user} onLogout={onLogout} onNavigate={onNavigate} />
      <main className="exams-content">
        <header className="exams-heading">
          <div>
            <p>ÁREA DO PACIENTE</p>
            <h1>Meus exames</h1>
          </div>
          <span><FileText size={16} aria-hidden="true" /> Fotos e resultados em um só lugar</span>
        </header>

        {error && <p className="exams-message exams-error" role="alert">{error}</p>}
        {success && <p className="exams-message exams-success" role="status">{success}</p>}

        <section className="exam-upload-card">
          <div className="exam-section-heading">
            <span className="exams-empty-icon"><ImagePlus size={22} aria-hidden="true" /></span>
            <div>
              <h2>Adicionar exame</h2>
              <p>Envie uma foto, PDF ou imagem do resultado.</p>
            </div>
          </div>
          <form className="exam-upload-form" onSubmit={handleUpload}>
            <label>
              <span>Nome do exame</span>
              <input name="title" value={form.title} onChange={updateField} required maxLength={150} placeholder="Ex.: Hemograma" />
            </label>
            <label>
              <span>Data do exame</span>
              <input name="examDate" type="date" value={form.examDate} onChange={updateField} required max={new Date().toISOString().slice(0, 10)} />
            </label>
            <label>
              <span>Laboratório (opcional)</span>
              <input name="laboratory" value={form.laboratory} onChange={updateField} maxLength={150} placeholder="Ex.: Fleury" />
            </label>
            <label className="exam-file-input">
              <span>Arquivo</span>
              <input name="file" type="file" accept="image/jpeg,image/png,application/pdf" capture="environment" onChange={updateField} required />
              <small>{form.file?.name || 'JPEG, PNG ou PDF · até 10 MB'}</small>
            </label>
            <button className="exam-upload-button" type="submit" disabled={isUploading}>
              <Upload size={16} aria-hidden="true" />
              {isUploading ? 'Enviando...' : 'Adicionar à biblioteca'}
            </button>
          </form>
        </section>

        <section className="exam-library" aria-label="Biblioteca de exames">
          <div className="exam-library-heading">
            <div>
              <p>ARQUIVOS SALVOS</p>
              <h2>Minha biblioteca</h2>
            </div>
            <span>{exams.length} {exams.length === 1 ? 'exame' : 'exames'}</span>
          </div>
          {isLoading ? (
            <p className="exams-empty-text">Carregando exames...</p>
          ) : exams.length === 0 ? (
            <p className="exams-empty-text">Você ainda não adicionou nenhum exame.</p>
          ) : (
            <div className="exam-list">
              {exams.map((exam) => {
                const isBusy = busyExamId === exam.id
                const sharedIds = exam.sharedAppointmentIds || []
                return (
                  <article className="exam-item" key={exam.id}>
                    <div className="exam-item-icon"><FileText size={20} aria-hidden="true" /></div>
                    <div className="exam-item-info">
                      <h3>{exam.title}</h3>
                      <p>{formatDate(exam.examDate)}{exam.laboratory ? ` · ${exam.laboratory}` : ''}</p>
                      <small>{exam.fileName} · {(exam.sizeBytes / 1024 / 1024).toFixed(2)} MB</small>
                    </div>
                    <div className="exam-item-actions">
                      <button type="button" onClick={() => handleDownload(exam)} disabled={isBusy} title="Baixar exame">
                        <Download size={16} aria-hidden="true" /> Baixar
                      </button>
                      <button type="button" className="exam-delete-button" onClick={() => handleDelete(exam)} disabled={isBusy} title="Excluir exame">
                        <Trash2 size={16} aria-hidden="true" />
                      </button>
                    </div>
                    <div className="exam-sharing">
                      <span><Link2 size={14} aria-hidden="true" /> Compartilhar com consulta</span>
                      <div className="exam-sharing-controls">
                        <select
                          aria-label={`Compartilhar ${exam.title}`}
                          defaultValue=""
                          disabled={isBusy || shareableAppointments.length === 0}
                          onChange={(event) => {
                            handleShare(exam, event.target.value)
                            event.target.value = ''
                          }}
                        >
                          <option value="">Escolha uma consulta...</option>
                          {shareableAppointments.map((appointment) => (
                            <option key={appointment.id} value={appointment.id} disabled={sharedIds.includes(appointment.id)}>
                              {formatAppointment(appointment)}{sharedIds.includes(appointment.id) ? ' · compartilhado' : ''}
                            </option>
                          ))}
                        </select>
                      </div>
                      {sharedIds.length > 0 && (
                        <div className="exam-shared-list">
                          {sharedIds.map((appointmentId) => {
                            const appointment = appointments.find((item) => item.id === appointmentId)
                            return (
                              <span key={appointmentId}>
                                {appointment ? formatAppointment(appointment) : 'Consulta compartilhada'}
                                <button type="button" onClick={() => handleUnshare(exam, appointmentId)} disabled={isBusy} aria-label="Remover compartilhamento">
                                  <X size={13} aria-hidden="true" />
                                </button>
                              </span>
                            )
                          })}
                        </div>
                      )}
                    </div>
                  </article>
                )
              })}
            </div>
          )}
        </section>
      </main>
    </div>
  )
}

import { useState } from 'react'
import toast from 'react-hot-toast'

function ExportBar({ dump }) {
  const [copied, setCopied] = useState(false)

  const formatDumpText = () => {
    let text = `🧠 MindDump — ${new Date(dump.createdAt).toLocaleDateString()}\n\n`

    if (dump.urgent?.length > 0) {
      text += `🔴 URGENT\n${dump.urgent.map(i => `  → ${i}`).join('\n')}\n\n`
    }
    if (dump.thisWeek?.length > 0) {
      text += `🟡 THIS WEEK\n${dump.thisWeek.map(i => `  → ${i}`).join('\n')}\n\n`
    }
    if (dump.someday?.length > 0) {
      text += `🟢 SOMEDAY\n${dump.someday.map(i => `  → ${i}`).join('\n')}\n\n`
    }
    if (dump.ideas?.length > 0) {
      text += `💡 IDEAS\n${dump.ideas.map(i => `  → ${i}`).join('\n')}\n\n`
    }
    if (dump.insight) {
      text += `🧠 INSIGHT\n  ${dump.insight}\n`
    }

    return text
  }

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(formatDumpText())
      setCopied(true)
      toast.success('Copied to clipboard')
      setTimeout(() => setCopied(false), 2000)
    } catch {
      toast.error('Copy failed')
    }
  }

  const handleDownload = () => {
    const text = formatDumpText()
    const blob = new Blob([text], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `minddump-${dump.id}.txt`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    toast.success('Downloaded')
  }

  return (
    <div className="export-bar">
      <button
        className={`btn-ghost ${copied ? 'btn-ghost--success' : ''}`}
        onClick={handleCopy}
      >
        {copied ? '✓ Copied' : '📋 Copy'}
      </button>
      <button className="btn-ghost" onClick={handleDownload}>
        ↓ Download .txt
      </button>
    </div>
  )
}

export default ExportBar

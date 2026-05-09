import { useState, useMemo } from 'react'
import toast from 'react-hot-toast'
import { Copy, Check, Download } from 'lucide-react'

function ExportBar({ dump }) {
  const [copied, setCopied] = useState(false)

  const getTaskText = (item) => typeof item === 'string' ? item : item.task

  const formatDumpText = useMemo(() => {
    let text = `MindDump — ${new Date(dump.createdAt).toLocaleDateString()}\n\n`

    const sections = [
      { key: 'doFirst', label: 'DO FIRST' },
      { key: 'doNext', label: 'DO NEXT' },
      { key: 'later', label: 'LATER' },
      { key: 'capture', label: 'CAPTURE' },
    ]

    for (const { key, label } of sections) {
      const items = dump[key]
      if (items?.length > 0) {
        text += `${label}\n${items.map(i => `  → ${getTaskText(i)}`).join('\n')}\n\n`
      }
    }

    if (dump.insight) {
      text += `INSIGHT\n  ${dump.insight}\n\n`
    }

    if (dump.cognitiveLoad?.level) {
      text += `COGNITIVE LOAD: ${dump.cognitiveLoad.level} (${dump.cognitiveLoad.score}/100)\n`
    }

    return text
  }, [dump])

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(formatDumpText)
      setCopied(true)
      toast.success('Copied to clipboard')
      setTimeout(() => setCopied(false), 2000)
    } catch {
      toast.error('Copy failed')
    }
  }

  const handleDownload = () => {
    const blob = new Blob([formatDumpText], { type: 'text/plain' })
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
        {copied ? <><Check size={14} /> Copied</> : <><Copy size={14} /> Copy</>}
      </button>
      <button className="btn-ghost" onClick={handleDownload}>
        <Download size={14} /> Download .txt
      </button>
    </div>
  )
}

export default ExportBar

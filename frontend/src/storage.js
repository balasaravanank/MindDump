const STORAGE_KEY = 'minddump-dumps'

function getAll() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function save(dump) {
  const dumps = getAll()
  const entry = {
    ...dump,
    id: Date.now().toString(),
    createdAt: new Date().toISOString(),
  }
  dumps.unshift(entry)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(dumps))
  return entry
}

function getById(id) {
  return getAll().find(d => d.id === id) || null
}

function updateDump(id, updates) {
  const dumps = getAll()
  const index = dumps.findIndex(d => d.id === id)
  if (index === -1) return null
  dumps[index] = { ...dumps[index], ...updates }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(dumps))
  return dumps[index]
}

function getCount() {
  return getAll().length
}

export const dumpStorage = { getAll, save, getById, updateDump, getCount }

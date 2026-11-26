const TOKEN_KEY = 'token'
export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const setToken = (t: string) => localStorage.setItem(TOKEN_KEY, t)
export const clearToken = () => localStorage.removeItem(TOKEN_KEY)

async function request(path: string, options: RequestInit = {}) {
    const token = getToken()
    const res = await fetch(path, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
    })
    const body = await res.json().catch(() => ({}))
    if (!res.ok) {
        throw new Error(body.error ?? (Object.values(body).join(', ') || 'Request failed'))
    }
    return body
}

const post = (path: string, email: string, password: string) =>
    request(path, { method: 'POST', body: JSON.stringify({ email, password }) })

export const register = (email: string, password: string) => post('/api/auth/register', email, password)
export const login = (email: string, password: string) => post('/api/auth/login', email, password)
export const me = () => request('/api/me')
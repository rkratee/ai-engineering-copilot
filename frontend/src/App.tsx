import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { clearToken, getToken, login, me, register, setToken } from './api'

type Profile = { id: string; email: string }
type Notice = { text: string; kind: 'error' | 'success' }

export default function App() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [notice, setNotice] = useState<Notice | null>(null)
    const [profile, setProfile] = useState<Profile | null>(null)

    useEffect(() => {
        if (getToken()) {
            me().then(setProfile).catch(() => clearToken())
        }
    }, [])

    async function handleRegister() {
        try {
            await register(email, password)
            setNotice({ text: 'Registered. Now log in.', kind: 'success' })
        } catch (e) {
            setNotice({ text: (e as Error).message, kind: 'error' })
        }
    }

    async function handleLogin(e: FormEvent) {
        e.preventDefault()
        try {
            const data = await login(email, password)
            setToken(data.accessToken)
            setProfile(await me())
            setNotice(null)
        } catch (err) {
            setNotice({ text: (err as Error).message, kind: 'error' })
        }
    }

    function handleLogout() {
        clearToken()
        setProfile(null)
        setPassword('')
    }

    if (profile) {
        return (
            <div className="card">
                <div className="brand">
                    <div className="logo">AI</div>
                    <h1>Welcome back</h1>
                </div>
                <div className="profile">
                    Logged in as <strong>{profile.email}</strong> (id {profile.id})
                </div>
                <button className="btn-primary" onClick={handleLogout}>Log out</button>
            </div>
        )
    }

    return (
        <form className="card" onSubmit={handleLogin}>
            <div className="brand">
                <div className="logo">AI</div>
                <h1>AI Engineering Copilot</h1>
            </div>
            <p className="subtitle">Sign in to continue</p>
            <label className="field">
                <span>Email</span>
                <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
            </label>
            <label className="field">
                <span>Password</span>
                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
            </label>
            <button type="submit" className="btn-primary">Log in</button>
            <button type="button" className="btn-secondary" onClick={handleRegister}>Create account</button>
            {notice && <div className={`msg ${notice.kind}`}>{notice.text}</div>}
        </form>
    )
}
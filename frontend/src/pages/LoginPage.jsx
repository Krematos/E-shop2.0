import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './LoginPage.css';

const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(username, password);
      navigate('/');
    } catch (error) {
      setError(
        error.response?.data?.error || 'Chyba při přihlašování. Zkuste to znovu.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">

      {/* Pravá strana - formulář */}
      <div className="login-form-side">
        <h2 className="login-title">Přihlášení</h2>
        <p className="login-subtitle">Prosím, zadejte své údaje.</p>

        {error && (
          <div style={{
            backgroundColor: '#fee',
            color: '#c33',
            padding: '12px',
            borderRadius: '8px',
            marginBottom: '20px',
            fontSize: '0.9rem'
          }}>
            {error}
          </div>
        )}

        {/* Formulář */}
        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="username">Uživatelské jméno</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Uživatelské jméno"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Heslo</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
              disabled={loading}
            />
          </div>

          <div className="forgot-password">
            <a href="/reset-password">Zapomněli jste heslo?</a>
          </div>

          {/* Hlavní CTA - výrazné */}
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Přihlašování...' : 'Přihlásit se'}
          </button>
        </form>

        <div className="divider">
          <span>nebo</span>
        </div>

        {/* Přihlášení přes sociální sítě */}
        <div className="social-login">
          <button type="button" className="btn btn-social btn-google">
            <i className="icon-google">G</i> Přihlásit se přes Google
          </button>
          <button type="button" className="btn btn-social btn-facebook">
            <i className="icon-facebook">f</i> Přihlásit se přes Facebook
          </button>
        </div>

        <div className="register-link" style={{ marginTop: '20px', textAlign: 'center' }}>
          Nemáte účet? <Link to="/register">Registrujte se</Link>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;

import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { register } from '../services/authService';
import SEO from '../components/SEO';
import './LoginPage.css';

const RegisterPage = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [agreeTerms, setAgreeTerms] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirmPassword) {
      setError('Hesla se neshodují');
      return;
    }

    if (formData.password.length < 6) {
      setError('Heslo musí mít alespoň 6 znaků');
      return;
    }

    setLoading(true);

    try {
      await register({
        username: formData.username,
        email: formData.email,
        password: formData.password,
      });

      // Po úspěšné registraci přesměrujeme na login se zprávou
      navigate('/login', {
        state: {
          message: 'Registrace proběhla úspěšně! Nyní se můžete přihlásit.',
          redirect: location.state?.redirect,
        },
      });
    } catch (error) {
      if (!error.response) {
        // Síťová chyba (backend neběží, výpadek sítě)
        setError('Nepodařilo se připojit k serveru. Zkontrolujte připojení k internetu.');
      } else {
        const data = error.response.data;
        if (typeof data === 'string') {
          // Backend někdy vrací např. "Chyba: Email již existuje" nebo Spring chybu
          let msg = data;
          // Pokud je to Spring Generic Exception formát jako "Bad Request: [Validation failed... default message [Text]]"
          const match = data.match(/default message \[([^\]]+)\]/);
          if (match && match[1]) {
            msg = match[1];
          }
          setError(msg);
        } else if (data && typeof data === 'object') {
          // Spring Validation errors pole nebo objekt
          if (data.errors && Array.isArray(data.errors)) {
            setError(data.errors.map(err => err.defaultMessage || err.message || err).join(', '));
          } else {
            let msg = data.message || data.error || 'Při registraci došlo k chybě.';
            if (msg.includes('Validation failed') || msg.includes('default message')) {
              const match = msg.match(/default message \[([^\]]+)\]/);
              if (match && match[1]) {
                msg = match[1];
              }
            } else if (msg.includes('Failed to convert') || msg.includes('JSON parse error')) {
              msg = 'Neplatný formát odeslaných dat.';
            } else if (msg.includes('with root cause')) {
              // Vytáhnout čitelnou zprávu z root cause
              const parts = msg.split('with root cause');
              msg = parts[parts.length - 1].replace(/^[\s:]+/, '').trim();
            }
            // Odstranění technického názvu výjimky z textu (např. "CustomException: ...")
            if (msg.includes(': ')) {
              const subParts = msg.split(': ');
              msg = subParts[subParts.length - 1];
            }
            // Odstranění technických závorek Springu typu [pole=hodnota]
            if (msg.startsWith('[') && msg.endsWith(']')) {
              const innerText = msg.slice(1, -1);
              msg = innerText.replace(/[a-zA-Z0-9_]+=/g, '').trim();
            }
            setError(msg);
          }
        } else {
          setError(data?.message || data?.error || 'Při registraci došlo k neočekávané chybě. Zkuste to prosím znovu.');
        }
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <SEO title="Registrace nového účtu" noindex={true} />
      <div className="login-form-side">
        <h2 className="login-title">Vytvořit účet</h2>
        <p className="login-subtitle">Rychlá registrace bez zbytečných polí.</p>

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

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="username">Uživatelské jméno</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Zvolte uživatelské jméno"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">E-mail</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="vas@email.cz"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Heslo</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Alespoň 6 znaků"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">Potvrzení hesla</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="Zopakujte heslo"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group-checkbox" style={{ display: 'flex', alignItems: 'flex-start', margin: '15px 0' }}>
            <input
              type="checkbox"
              id="terms"
              checked={agreeTerms}
              onChange={(e) => setAgreeTerms(e.target.checked)}
              required
              style={{ marginTop: '5px', marginRight: '10px', cursor: 'pointer' }}
              disabled={loading}
            />
            <label htmlFor="terms" style={{ display: 'inline', fontWeight: 'normal', fontSize: '0.9rem', color: '#555', cursor: 'pointer' }}>
              Souhlasím s <Link to="/terms" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--primary-color, #007bff)', textDecoration: 'underline' }}>Obchodními podmínkami</Link> a <Link to="/privacy" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--primary-color, #007bff)', textDecoration: 'underline' }}>Zásadami ochrany osobních údajů</Link>.
            </label>
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Registruji...' : 'Registrovat se'}
          </button>
        </form>

        <div className="register-link" style={{ marginTop: '20px', textAlign: 'center' }}>
          Již máte účet? <Link to="/login" state={location.state}>Přihlaste se</Link>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;

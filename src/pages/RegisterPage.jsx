import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { register } from '../services/authService';
import './LoginPage.css';

const RegisterPage = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
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
      alert('Registrace proběhla úspěšně! Nyní se můžete přihlásit.');
      navigate('/login', { state: location.state });
    } catch (error) {
      if (!error.response) {
        setError('Nepodařilo se připojit k serveru. Zkontrolujte připojení k internetu.');
      } else {
        const { status, data } = error.response;
        
        if (status === 409) {
          setError('Uživatel s tímto uživatelským jménem nebo e-mailem již existuje.');
        } else if (status === 400) {
          if (data && typeof data === 'object' && !data.error && !data.message) {
            const validationMessages = Object.values(data).filter(msg => typeof msg === 'string');
            setError(validationMessages.length > 0 ? validationMessages.join(' • ') : 'Zadaná data nejsou platná.');
          } else {
            let msg = data?.message || data?.error || 'Zadaná data nejsou platná. Zkontrolujte je prosím.';
            if (typeof msg === 'string' && msg.includes('{') && msg.includes('}')) {
              const innerText = msg.substring(msg.indexOf('{') + 1, msg.lastIndexOf('}'));
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

        {/* Přihlášení přes sociální sítě */}
        <div className="social-login" style={{ marginBottom: '30px' }}>
          <button type="button" className="btn btn-social btn-google flex items-center justify-center gap-2">
            <svg className="w-5 h-5 mr-1" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
              <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
              <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/>
              <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/>
            </svg>
            Registrovat se přes Google
          </button>
          <button type="button" className="btn btn-social btn-facebook flex items-center justify-center gap-2">
            <svg className="w-5 h-5 mr-1" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
              <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
            </svg>
            Registrovat se přes Facebook
          </button>
        </div>

        <div className="divider">
          <span>nebo s emailem</span>
        </div>

        {/* Formulář */}
        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="username">Uživatelské jméno</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Uživatelské jméno"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="vasedres@email.cz"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Heslo (min. 6 znaků)</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="••••••••"
              minLength="6"
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
              placeholder="••••••••"
              required
              disabled={loading}
            />
          </div>

          {/* Checkbox pro souhlas s podmínkami */}
          <div className="form-group" style={{ display: 'flex', alignItems: 'flex-start', marginBottom: '20px' }}>
            <input 
              type="checkbox" 
              id="terms" 
              required 
              style={{ marginTop: '5px', marginRight: '10px', cursor: 'pointer' }}
              disabled={loading}
            />
            <label htmlFor="terms" style={{ display: 'inline', fontWeight: 'normal', fontSize: '0.9rem', color: '#555', cursor: 'pointer' }}>
              Souhlasím s <Link to="/about" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--primary-color, #007bff)', textDecoration: 'underline' }}>Obchodními podmínkami</Link> a <Link to="/about" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--primary-color, #007bff)', textDecoration: 'underline' }}>Zásadami ochrany osobních údajů</Link>.
            </label>
          </div>

          {/* Hlavní CTA - akcentní barva */}
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Registrace...' : 'Registrovat se'}
          </button>
        </form>

        <div className="register-link" style={{ marginTop: '20px', textAlign: 'center' }}>
          Už máte účet? <Link to="/login" state={location.state}>Přihlaste se</Link>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;

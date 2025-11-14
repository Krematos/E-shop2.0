import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
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
      navigate('/login');
    } catch (error) {
      setError(
        error.response?.data?.error || 'Chyba při registraci. Zkuste to znovu.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">

      {/* Formulářová část - Registrace */}
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

        {/* Přihlášení přes sociální sítě - před formulářem pro rychlost */}
        <div className="social-login" style={{ marginBottom: '30px' }}>
          <button type="button" className="btn btn-social btn-google">
            <i className="icon-google">G</i> Registrovat se přes Google
          </button>
          <button type="button" className="btn btn-social btn-facebook">
            <i className="icon-facebook">f</i> Registrovat se přes Facebook
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
              style={{ marginTop: '5px', marginRight: '10px' }}
              disabled={loading}
            />
            <label htmlFor="terms" style={{ display: 'inline', fontWeight: 'normal', fontSize: '0.9rem' }}>
              Souhlasím s <a href="/terms" target="_blank" rel="noopener noreferrer">Obchodními podmínkami</a> a <a href="/privacy" target="_blank" rel="noopener noreferrer">Zásadami ochrany osobních údajů</a>.
            </label>
          </div>

          {/* Hlavní CTA - akcentní barva */}
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Registrace...' : 'Registrovat se'}
          </button>
        </form>

        <div className="register-link" style={{ marginTop: '20px', textAlign: 'center' }}>
          Už máte účet? <Link to="/login">Přihlaste se</Link>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;

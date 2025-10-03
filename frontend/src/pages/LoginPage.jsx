import React, { useState } from 'react';
import './LoginPage.css'; // Importujeme styly

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    // Zde by proběhla logická validace a přihlášení
    console.log('Přihlašovací údaje:', { email, password });
  };

  return (
    <div className="login-container">
      {/* Vlevo jen logo/obrázek na desktopu, na mobilu pryč */}
      <div className="login-visual-side">
        <h1 className="logo">SecondEL</h1>
        <p>Vítejte zpět! Nakupování nebylo nikdy jednodušší.</p>
      </div>

      {/* Pravá strana - formulář */}
      <div className="login-form-side">
        <h2 className="login-title">Přihlášení</h2>
        <p className="login-subtitle">Prosím, zadejte své údaje.</p>

        {/* Formulář */}
        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="vasedres@email.cz"
              required
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
            />
          </div>

          <div className="forgot-password">
            <a href="/reset-password">Zapomněli jste heslo?</a>
          </div>

          {/* Hlavní CTA - výrazné */}
          <button type="submit" className="btn btn-primary">
            Přihlásit se
          </button>
        </form>

        <div className="divider">
          <span>nebo</span>
        </div>

        {/* Přihlášení přes sociální sítě */}
        <div className="social-login">
          <button className="btn btn-social btn-google">
            <i className="icon-google">G</i> Přihlásit se přes Google
          </button>
          <button className="btn btn-social btn-facebook">
            <i className="icon-facebook">f</i> Přihlásit se přes Facebook
          </button>
        </div>

        <div className="register-link">
          Nemáte účet? <a href="/register">Registrujte se</a>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
import React, { useState } from 'react';
import './LoginPage.css';

const RegisterPage = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password !== passwordConfirm) {
      alert('Hesla se neshodují!');
      return;
    }
    try {
        const response = await fetch("http://localhost:8080/api/auth/register", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ name, email, password })
        });

        if (response.ok) {
          alert("Registrace proběhla úspěšně ✅");
        } else {
          const err = await response.json();
          alert("Chyba: " + err.message);
        }
      } catch (error) {
        console.error("Chyba při registraci:", error);
        alert("Nepodařilo se spojit s backendem ❌");
      }
    };

  return (
    <div className="login-container">
      {/* Vizuální strana - stejná pro konzistenci */}
      <div className="login-visual-side">
        <h1 className="logo">SecondEL</h1>
        <p>Začněte nakupovat dnes! Rychlá registrace a spousta výhod.</p>
      </div>

      {/* Formulářová část - Rejstřík */}
      <div className="login-form-side">
        <h2 className="login-title">Vytvořit účet</h2>

        {/* Přihlášení přes sociální sítě - před formulářem pro rychlost */}
        <div className="social-login" style={{ marginBottom: '30px' }}>
          <button className="btn btn-social btn-google">
            <i className="icon-google">G</i> Registrovat se přes Google
          </button>
          <button className="btn btn-social btn-facebook">
            <i className="icon-facebook">f</i> Registrovat se přes Facebook
          </button>
        </div>

        <div className="divider">
          <span>nebo s emailem</span>
        </div>

        {/* Formulář */}
        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="name">Jméno</label>
            <input
              type="text"
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Jan Novák"
              required
            />
          </div>


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
            <label htmlFor="password">Heslo (min. 8 znaků)</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              minLength="8"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password-confirm">Potvrzení hesla</label>
            <input
              type="password"
              id="password-confirm"
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>

          {/* Checkbox pro souhlas s podmínkami - klíčový právní prvek */}
          <div className="form-group terms-checkbox">
            <input type="checkbox" id="terms" required />
            <label htmlFor="terms" style={{ display: 'inline', marginLeft: '10px' }}>
              Souhlasím s <a href="/terms" target="_blank">Obchodními podmínkami</a> a <a href="/privacy" target="_blank">Zásadami ochrany osobních údajů</a>.
            </label>
          </div>

          {/* Hlavní CTA - akcentní barva */}
          <button type="submit" className="btn btn-primary">
            Registrovat se
          </button>
        </form>

        <div className="register-link" style={{ marginTop: '20px' }}>
          Už máte účet? <a href="/login">Přihlaste se</a>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
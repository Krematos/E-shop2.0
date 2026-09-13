import React, { useState } from 'react';
import { Link } from 'react-router-dom';

const ResetPasswordPage = () => {
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');

    if (password !== confirmPassword) {
      setError('Hesla se neshodují.');
      return;
    }

    try {
      setMessage('Vaše heslo bylo úspěšně obnoveno!');
      setPassword('');
      setConfirmPassword('');

    } catch (error) {
      console.error('Chyba při obnově hesla:', error);
      if (error && !error.response) {
        setError('Nepodařilo se připojit k serveru. Zkontrolujte připojení k internetu.');
      } else {
        setError(error?.response?.data?.message || error?.response?.data?.error || 'Došlo k neočekávané chybě. Zkuste to prosím později.');
      }
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <h2 className="text-2xl font-bold text-center mb-6">Obnovení hesla</h2>
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label htmlFor="password" className="block text-gray-700 text-sm font-bold mb-2">
              Nové heslo
            </label>
            <input
              type="password"
              id="password"
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              placeholder="Zadejte nové heslo"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <div className="mb-6">
            <label htmlFor="confirmPassword" className="block text-gray-700 text-sm font-bold mb-2">
              Potvrďte nové heslo
            </label>
            <input
              type="password"
              id="confirmPassword"
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              placeholder="Zadejte heslo znovu"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>
          {message && <p className="text-green-500 text-center mb-4">{message}</p>}
          {error && <p className="text-red-500 text-center mb-4">{error}</p>}
          <div className="flex items-center justify-between">
            <button
              type="submit"
              className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline w-full"
            >
              Uložit nové heslo
            </button>
          </div>
          <p className="text-center text-gray-600 text-sm mt-4">
            Vzpomněli jste si na heslo? <Link to="/login" className="text-blue-500 hover:text-blue-800">Přihlaste se</Link>
          </p>
        </form>
      </div>
    </div>
  );
};

export default ResetPasswordPage;

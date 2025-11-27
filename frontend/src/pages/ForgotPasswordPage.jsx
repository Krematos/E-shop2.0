import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { requestPasswordReset } from '../services/authService';

const ForgotPasswordPage = () => {
  const [email, setEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState(null); // Úspěšná zpráva
  const [error, setError] = useState(null);     // Chybová zpráva

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);
    setMessage(null);

    try {
      // Zavoláme API
      await requestPasswordReset(email);

      // Úspěch
      setMessage('Pokud je tento e-mail registrován, byl na něj odeslán odkaz pro obnovu hesla.');
      setEmail(''); // Vyčistíme pole
    } catch (err) {
      // Chyba (např. server neodpovídá)
      setError('Chyba při odesílání požadavku. Zkuste to prosím později.');
      console.error(err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Zapomněli jste heslo?
        </h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          Zadejte svůj e-mail a my vám pošleme odkaz pro obnovení.
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">

          {/* Úspěšná zpráva */}
          {message ? (
            <div className="rounded-md bg-green-50 p-4 mb-4 border border-green-200">
              <div className="flex">
                <div className="flex-shrink-0">
                  <span className="text-green-400">✅</span>
                </div>
                <div className="ml-3">
                  <p className="text-sm font-medium text-green-800">{message}</p>
                </div>
              </div>
              <div className="mt-4 text-center">
                <Link to="/login" className="text-sm font-medium text-green-600 hover:text-green-500">
                  Zpět na přihlášení
                </Link>
              </div>
            </div>
          ) : (
            /* Formulář (zobrazí se, jen když není odesláno) */
            <form className="space-y-6" onSubmit={handleSubmit}>

              {/* Chybová zpráva */}
              {error && (
                <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded text-sm">
                  {error}
                </div>
              )}

              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                  E-mailová adresa
                </label>
                <div className="mt-1">
                  <input
                    id="email"
                    name="email"
                    type="email"
                    autoComplete="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    placeholder="vas@email.cz"
                  />
                </div>
              </div>

              <div>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isSubmitting ? 'Odesílám...' : 'Odeslat odkaz'}
                </button>
              </div>
            </form>
          )}

          {/* Odkaz zpět, pokud ještě není odesláno */}
          {!message && (
            <div className="mt-6">
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-300" />
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-2 bg-white text-gray-500">
                    Nebo
                  </span>
                </div>
              </div>

              <div className="mt-6 text-center">
                <Link to="/login" className="font-medium text-blue-600 hover:text-blue-500">
                  Zpět na přihlášení
                </Link>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;
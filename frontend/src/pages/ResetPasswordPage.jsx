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
      setError('Passwords do not match.');
      return;
    }

    // In a real application, you would extract a token from the URL
    // and send it along with the new password to your backend API.
    // Example: const token = new URLSearchParams(window.location.search).get('token');
    // if (!token) {
    //   setError('Password reset token is missing.');
    //   return;
    // }

    try {
      // Replace with your actual API call
      // const response = await fetch('/api/reset-password', {
      //   method: 'POST',
      //   headers: {
      //     'Content-Type': 'application/json',
      //   },
      //   body: JSON.stringify({ token, newPassword: password }),
      // });

      // const data = await response.json();

      // if (response.ok) {
      //   setMessage(data.message || 'Your password has been reset successfully!');
      //   setPassword('');
      //   setConfirmPassword('');
      // } else {
      //   setError(data.message || 'Failed to reset password. Please try again.');
      // }

      // Mock success for demonstration
      setMessage('Your password has been reset successfully!');
      setPassword('');
      setConfirmPassword('');

    } catch (err) {
      setError('An unexpected error occurred. Please try again later.');
      console.error('Reset password error:', err);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <h2 className="text-2xl font-bold text-center mb-6">Reset Password</h2>
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label htmlFor="password" className="block text-gray-700 text-sm font-bold mb-2">
              New Password
            </label>
            <input
              type="password"
              id="password"
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              placeholder="Enter new password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <div className="mb-6">
            <label htmlFor="confirmPassword" className="block text-gray-700 text-sm font-bold mb-2">
              Confirm New Password
            </label>
            <input
              type="password"
              id="confirmPassword"
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              placeholder="Confirm new password"
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
              Reset Password
            </button>
          </div>
          <p className="text-center text-gray-600 text-sm mt-4">
            Remember your password? <Link to="/login" className="text-blue-500 hover:text-blue-800">Login</Link>
          </p>
        </form>
      </div>
    </div>
  );
};

export default ResetPasswordPage;

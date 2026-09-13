import React, { Suspense, lazy, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import api from './services/api';
import { AuthProvider } from './context/AuthProvider';
import { CartProvider } from './context/CartProvider';
import Header from './components/Header';
import Footer from './components/Footer';
import LoadingSpinner from './components/LoadingSpinner';
import { CookieConsentProvider } from './context/CookieConsentProvider';
import CookieConsentBanner from './components/CookieConsentBanner';

// Dynamické importy stránek (Code Splitting podle routy)
const HomePage = lazy(() => import('./pages/HomePage'));
const ProductListPage = lazy(() => import('./pages/ProductListPage'));
const ProductDetailPage = lazy(() => import('./pages/ProductDetailPage'));
const CartPage = lazy(() => import('./pages/CartPage'));
const LoginPage = lazy(() => import('./pages/LoginPage'));
const RegisterPage = lazy(() => import('./pages/RegisterPage'));
const ResetPasswordPage = lazy(() => import('./pages/ResetPasswordPage'));
const ProfilePage = lazy(() => import('./pages/ProfilePage'));
const AdminPage = lazy(() => import('./pages/AdminPage'));
const AdminOrdersPage = lazy(() => import('./pages/AdminOrdersPage'));
const About = lazy(() => import('./pages/About'));
const ForgotPasswordPage = lazy(() => import('./pages/ForgotPasswordPage'));
const CheckoutPage = lazy(() => import('./pages/CheckoutPage'));
const CheckoutPaymentPage = lazy(() => import('./pages/CheckoutPaymentPage'));
const CheckoutConfirmPage = lazy(() => import('./pages/CheckoutConfirmPage'));
const TermsPage = lazy(() => import('./pages/TermsPage'));
const PrivacyPage = lazy(() => import('./pages/PrivacyPage'));

function App() {
  useEffect(() => {
    // Hned po spuštění načteme CSRF token z nového endpointu /api/csrf/token.
    // Backend vrátí objekt { token, headerName, parameterName } .
    // Axios si token automaticky přečte z cookie XSRF-TOKEN a přidá ho do headeru.
    api.get('/csrf/token')
      .then(() => console.log('CSRF token úspěšně načten'))
      .catch(err => console.error('Chyba při získávání CSRF:', err));
  }, []);
  return (
    <AuthProvider>
      <CartProvider>
        <CookieConsentProvider>
          <Router>
            <div className="flex flex-col min-h-screen">
              <Header />
              <main className="flex-grow flex flex-col">
                <Suspense fallback={<div className="flex-grow flex items-center justify-center"><LoadingSpinner /></div>}>
                  <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/products" element={<ProductListPage />} />
                    <Route path="/products/:id" element={<ProductDetailPage />} />
                    <Route path="/cart" element={<CartPage />} />
                    <Route path="/checkout" element={<CheckoutPage />} />
                    <Route path="/checkout/payment" element={<CheckoutPaymentPage />} />
                    <Route path="/checkout/confirm" element={<CheckoutConfirmPage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/profile" element={<ProfilePage />} />
                    <Route path="/admin" element={<AdminPage />} />
                    <Route path="/admin/orders" element={<AdminOrdersPage />} />
                    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                    <Route path="/about" element={<About />} />
                    <Route path="/terms" element={<TermsPage />} />
                    <Route path="/privacy" element={<PrivacyPage />} />
                  </Routes>
                </Suspense>
              </main>
              <Footer />
              <CookieConsentBanner />
            </div>
          </Router>
        </CookieConsentProvider>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;

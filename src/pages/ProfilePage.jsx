import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getUserOrders } from '../services/orderService';
import { deleteAccount } from '../services/userService';
import { sendWebhook } from '../services/webhookService';
import ProtectedRoute from '../components/ProtectedRoute';
import LoadingSpinner from '../components/LoadingSpinner';

const ProfilePage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const data = await getUserOrders();
        setOrders(data);
      } catch (error) {
        console.error('Chyba při načítání objednávek:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, []);

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('cs-CZ', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handleDeleteAccount = async () => {
    setDeleteLoading(true);
    setDeleteError('');
    try {
      await deleteAccount();
      await sendWebhook('account_deleted', { username: user?.username });
      logout();
      navigate('/');
    } catch (error) {
      console.error('Chyba při mazání účtu:', error);
      setDeleteError('Nepodařilo se smazat účet. Zkuste to prosím znovu.');
      setDeleteLoading(false);
    }
  };

  return (
    <ProtectedRoute>
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold mb-6">Profil uživatele</h1>

        {/* Informace o uživateli */}
        <div className="card mb-8">
          <h2 className="text-2xl font-semibold mb-4">Informace o účtu</h2>
          <div className="space-y-2">
            <p>
              <span className="font-semibold">Uživatelské jméno:</span>{' '}
              {user?.username}
            </p>
            <p>
              <span className="font-semibold">Role:</span>{' '}
              {user?.roles?.map((r) => {
                const role = typeof r === 'string' ? r : r.authority;
                return role === 'ROLE_ADMIN' ? 'Administrátor' : 'Uživatel';
              }).join(', ') || 'Uživatel'}
            </p>
          </div>

          {/* Tlačítko smazat účet */}
          <div style={{ marginTop: '1.5rem', paddingTop: '1.5rem', borderTop: '1px solid #e5e7eb' }}>
            <h3 style={{ fontSize: '1rem', fontWeight: '600', color: '#374151', marginBottom: '0.5rem' }}>
              Nebezpečná zóna
            </h3>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '1rem' }}>
              Smazání účtu je nevratná akce. Všechna vaše data budou trvale odstraněna.
            </p>
            <button
              id="btn-delete-account"
              onClick={() => setShowDeleteModal(true)}
              style={{
                backgroundColor: '#dc2626',
                color: '#ffffff',
                padding: '0.5rem 1.25rem',
                borderRadius: '0.375rem',
                border: 'none',
                cursor: 'pointer',
                fontWeight: '600',
                fontSize: '0.875rem',
                transition: 'background-color 0.2s ease',
              }}
              onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = '#b91c1c'; }}
              onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = '#dc2626'; }}
            >
              Smazat účet
            </button>
          </div>
        </div>

        {/* Historie objednávek */}
        <div className="card">
          <h2 className="text-2xl font-semibold mb-4">Historie objednávek</h2>
          {loading ? (
            <LoadingSpinner />
          ) : orders.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="text-left py-2">ID objednávky</th>
                    <th className="text-left py-2">Produkt</th>
                    <th className="text-left py-2">Množství</th>
                    <th className="text-left py-2">Cena</th>
                    <th className="text-left py-2">Datum</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((order) => {
                    const price = typeof order.Price === 'string'
                      ? Number.parseFloat(order.Price)
                      : order.Price || order.totalPrice || 0;
                    const totalPrice = price * (order.quantity || 1);

                    return (
                      <tr key={order.id} className="border-b">
                        <td className="py-2">#{order.id}</td>
                        <td className="py-2">{order.productName}</td>
                        <td className="py-2">{order.quantity}</td>
                        <td className="py-2">{totalPrice.toFixed(2)} Kč</td>
                        <td className="py-2">
                          {formatDate(order.createdAt)}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-gray-500 text-center py-8">
              Zatím nemáte žádné objednávky
            </p>
          )}
        </div>
      </div>

      {/* Potvrzovací modal */}
      {showDeleteModal && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
        >
          <div
            style={{
              backgroundColor: '#ffffff',
              borderRadius: '0.75rem',
              padding: '2rem',
              maxWidth: '28rem',
              width: '90%',
              boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
            }}
          >
            <h2 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '0.75rem', color: '#111827' }}>
              ⚠️ Smazat účet?
            </h2>
            <p style={{ color: '#6b7280', marginBottom: '1.5rem', lineHeight: '1.6' }}>
              Opravdu chcete smazat svůj účet <strong>{user?.username}</strong>?
              Tato akce je <strong>nevratná</strong> a všechna vaše data budou trvale odstraněna.
            </p>

            {deleteError && (
              <p style={{ color: '#dc2626', marginBottom: '1rem', fontSize: '0.875rem' }}>
                {deleteError}
              </p>
            )}

            <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
              <button
                id="btn-cancel-delete"
                onClick={() => { setShowDeleteModal(false); setDeleteError(''); }}
                disabled={deleteLoading}
                style={{
                  padding: '0.5rem 1.25rem',
                  borderRadius: '0.375rem',
                  border: '1px solid #d1d5db',
                  backgroundColor: '#ffffff',
                  color: '#374151',
                  cursor: 'pointer',
                  fontWeight: '500',
                }}
              >
                Zrušit
              </button>
              <button
                id="btn-confirm-delete"
                onClick={handleDeleteAccount}
                disabled={deleteLoading}
                style={{
                  padding: '0.5rem 1.25rem',
                  borderRadius: '0.375rem',
                  border: 'none',
                  backgroundColor: deleteLoading ? '#fca5a5' : '#dc2626',
                  color: '#ffffff',
                  cursor: deleteLoading ? 'not-allowed' : 'pointer',
                  fontWeight: '600',
                }}
              >
                {deleteLoading ? 'Mazání...' : 'Ano, smazat účet'}
              </button>
            </div>
          </div>
        </div>
      )}
    </ProtectedRoute>
  );
};

export default ProfilePage;

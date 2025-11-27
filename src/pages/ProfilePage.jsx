import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { getUserOrders } from '../services/orderService';
import ProtectedRoute from '../components/ProtectedRoute';
import LoadingSpinner from '../components/LoadingSpinner';

const ProfilePage = () => {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

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
                      ? parseFloat(order.Price) 
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
    </ProtectedRoute>
  );
};

export default ProfilePage;


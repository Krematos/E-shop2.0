import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { createOrder } from '../services/orderService';
import LoadingSpinner from '../components/LoadingSpinner';
import { useState } from 'react';
import { getImageUrl } from '../utils/urlUtils';

const CartPage = () => {
  const { cartItems, updateQuantity, removeFromCart, getTotalPrice, clearCart } = useCart();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const handleCheckout = async () => {
    if (!isAuthenticated()) {
      navigate('/login');
      return;
    }

    if (cartItems.length === 0) {
      alert('Košík je prázdný');
      return;
    }

    setLoading(true);
    try {
      // Příprava dat pro backend (CreateOrderRequest)
      const orderItems = cartItems.map(item => ({
        productId: item.id,
        quantity: item.quantity
      }));

      await createOrder({
        orderItems: orderItems
      });

      clearCart();
      alert('Objednávka byla úspěšně vytvořena!');
      navigate('/profile');
    } catch (error) {
      console.error('Chyba při vytváření objednávky:', error);
      alert('Chyba při vytváření objednávky. Zkuste to znovu.');
    } finally {
      setLoading(false);
    }
  };

  if (cartItems.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold mb-6">Košík</h1>
        <div className="card text-center py-12">
          <p className="text-gray-500 text-lg mb-4">Váš košík je prázdný</p>
          <button
            onClick={() => navigate('/products')}
            className="btn-primary"
          >
            Procházet produkty
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Košík</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Seznam produktů */}
        <div className="lg:col-span-2 space-y-4">
          {cartItems.map((item) => {
            const price = typeof item.price === 'string'
              ? Number.parseFloat(item.price)
              : item.price;
            const itemTotal = price * item.quantity;

            return (
              <div key={item.id} className="card flex flex-col md:flex-row gap-4">
                <div className="w-full md:w-32 h-32 bg-gray-200 rounded-lg flex items-center justify-center flex-shrink-0">
                  {item.images?.[0] ? (
                    <img
                      src={getImageUrl(item.images[0])}
                      alt={item.name}
                      className="w-full h-full object-cover rounded-lg"
                    />
                  ) : (
                    <div className="text-gray-400 text-4xl">📦</div>
                  )}
                </div>

                <div className="flex-1">
                  <h3 className="text-xl font-semibold mb-2">{item.name}</h3>
                  <p className="text-gray-600 mb-4">{price?.toFixed(2) || '0.00'} Kč za kus</p>

                  <div className="flex items-center space-x-4">
                    <label className="text-sm font-semibold">Množství:</label>
                    <button
                      onClick={() => updateQuantity(item.id, item.quantity - 1)}
                      className="bg-gray-200 hover:bg-gray-300 px-3 py-1 rounded"
                    >
                      -
                    </button>
                    <span className="w-12 text-center">{item.quantity}</span>
                    <button
                      onClick={() => updateQuantity(item.id, item.quantity + 1)}
                      className="bg-gray-200 hover:bg-gray-300 px-3 py-1 rounded"
                    >
                      +
                    </button>
                    <button
                      onClick={() => removeFromCart(item.id)}
                      className="text-red-600 hover:text-red-700 ml-4"
                    >
                      Odstranit
                    </button>
                  </div>
                </div>

                <div className="text-right">
                  <p className="text-xl font-bold text-primary-600">
                    {itemTotal.toFixed(2)} Kč
                  </p>
                </div>
              </div>
            );
          })}
        </div>

        {/* Souhrn objednávky */}
        <div className="lg:col-span-1">
          <div className="card sticky top-24">
            <h2 className="text-2xl font-bold mb-4">Souhrn objednávky</h2>
            <div className="space-y-2 mb-6">
              <div className="flex justify-between">
                <span>Mezisoučet:</span>
                <span>{getTotalPrice().toFixed(2)} Kč</span>
              </div>
              <div className="flex justify-between">
                <span>DPH:</span>
                <span>{(getTotalPrice() * 0.21).toFixed(2)} Kč</span>
              </div>
              <div className="border-t pt-2 flex justify-between text-xl font-bold">
                <span>Celkem:</span>
                <span className="text-primary-600">
                  {(getTotalPrice() * 1.21).toFixed(2)} Kč
                </span>
              </div>
            </div>

            {loading ? (
              <LoadingSpinner />
            ) : (
              <button
                onClick={handleCheckout}
                className="btn-primary w-full text-lg py-3"
              >
                Objednat
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CartPage;


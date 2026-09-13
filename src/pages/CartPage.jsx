import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/useCart';
import { useAuth } from '../context/useAuth';
import { getImageUrl, getResponsiveSrcSet } from '../utils/urlUtils';
import { ShoppingCart } from 'lucide-react';

const CartPage = () => {
  const { cartItems, updateQuantity, removeFromCart, getTotalPrice } = useCart();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleCheckout = () => {
    if (isAuthenticated()) {
      navigate('/checkout');
    } else {
      navigate('/login', { state: { redirect: '/checkout' } })
    }
  };

  if (cartItems.length === 0) {
    return (
      <div className="container mx-auto px-4 py-12 max-w-4xl">
        <h1 className="text-4xl font-extrabold mb-8 text-gray-900 tracking-tight">Košík</h1>
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 text-center py-16 px-6">
          <ShoppingCart className="mx-auto h-16 w-16 text-gray-400 mb-6 stroke-1" />
          <h2 className="text-2xl font-bold text-gray-800 mb-2">Váš košík zeje prázdnotou</h2>
          <p className="text-gray-500 mb-8 max-w-md mx-auto">Vyberte si něco hezkého z naší nabídky, zásoby se tenčí!</p>
          <button
            onClick={() => navigate('/')}
            className="bg-blue-600 text-white hover:bg-blue-700 font-semibold py-3 px-8 rounded-full transition-all hover:shadow-lg active:scale-95"
          >
            Procházet produkty
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 lg:py-12 max-w-6xl">
      <h1 className="text-3xl font-extrabold mb-8 text-gray-900 tracking-tight">Váš nákupní košík</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Seznam produktů */}
        <div className="lg:col-span-2 space-y-4">
          {cartItems.map((item) => {
            const price = typeof item.price === 'string'
              ? Number.parseFloat(item.price)
              : item.price;
            const itemTotal = price * item.quantity;
            
            const mainImageFilename = item?.mainImage || item?.mainImageUrl || item?.images?.find?.(img => img?.main || img?.isMain)?.url || item?.images?.[0]?.url || item?.images?.[0];

            return (
              <div key={item.id} className="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-6 items-center transition-shadow hover:shadow-md">
                <div className="w-full md:w-32 h-32 bg-gray-50 rounded-xl flex items-center justify-center flex-shrink-0 overflow-hidden">
                  {mainImageFilename ? (
                    <img
                      src={getImageUrl(mainImageFilename)}
                      srcSet={getResponsiveSrcSet(mainImageFilename)}
                      sizes="128px"
                      width="128"
                      height="128"
                      alt={item.name}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className="text-gray-300 text-4xl">📦</div>
                  )}
                </div>

                <div className="flex-1 w-full">
                  <h3 className="text-lg font-bold text-gray-800 mb-1 line-clamp-2">{item.name}</h3>
                  <p className="text-gray-500 text-sm mb-4">{price?.toFixed(2) || '0.00'} Kč za kus</p>

                  <div className="flex flex-wrap items-center gap-4">
                    <div className="flex items-center bg-gray-100 rounded-lg p-1">
                      <button
                        onClick={() => updateQuantity(item.id, item.quantity - 1)}
                        className="w-8 h-8 flex items-center justify-center rounded-md bg-white text-gray-700 shadow-sm hover:text-blue-600 transition-colors"
                      >
                        -
                      </button>
                      <span className="w-10 text-center font-medium text-gray-700">{item.quantity}</span>
                      <button
                        onClick={() => updateQuantity(item.id, item.quantity + 1)}
                        className="w-8 h-8 flex items-center justify-center rounded-md bg-white text-gray-700 shadow-sm hover:text-blue-600 transition-colors"
                      >
                        +
                      </button>
                    </div>
                    <button
                      onClick={() => removeFromCart(item.id)}
                      className="text-gray-400 hover:text-red-500 text-sm font-medium transition-colors"
                    >
                      Odebrat
                    </button>
                  </div>
                </div>

                <div className="text-right w-full md:w-auto mt-4 md:mt-0">
                  <p className="text-xl font-extrabold text-blue-600 whitespace-nowrap">
                    {itemTotal.toFixed(2)} Kč
                  </p>
                </div>
              </div>
            );
          })}
        </div>

        {/* Souhrn objednávky */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 sticky top-24">
            <h2 className="text-xl font-bold mb-6 text-gray-800">Shrnutí objednávky</h2>
            <div className="space-y-4 mb-8 text-sm text-gray-600 mt-4">
              <div className="flex justify-between items-center">
                <span>Položky celkem:</span>
                <span className="font-medium text-gray-900">{cartItems.reduce((t, i) => t + i.quantity, 0)} ks</span>
              </div>
              <div className="flex justify-between items-center">
                <span>Doprava a balné:</span>
                <span className="font-medium text-green-600">Zdarma</span>
              </div>
              <div className="pt-4 border-t border-gray-100 flex justify-between items-end">
                <div>
                  <span className="block text-lg font-bold text-gray-900">Celkem k úhradě</span>
                  <span className="block text-xs text-gray-400 mt-1">Včetně DPH a všech poplatků</span>
                </div>
                <span className="text-3xl font-extrabold text-blue-600">
                  {getTotalPrice().toFixed(2)} Kč
                </span>
              </div>
            </div>

            <button
              onClick={handleCheckout}
              className="w-full bg-blue-600 text-white font-semibold py-4 px-6 rounded-xl hover:bg-blue-700 transition-all hover:shadow-lg active:scale-95 text-lg"
            >
              Přejít k objednávce
            </button>
            
            <p className="text-xs text-gray-400 text-center mt-4 flex items-center justify-center gap-2">
              🔒 Bezpečná platba zaručena
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CartPage;

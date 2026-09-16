import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/useCart';
import { getImageUrl, getResponsiveSrcSet, defaultImageSizes } from '../utils/urlUtils';
import SEO from '../components/SEO';

const CartPage = () => {
  const { cartItems, removeFromCart, updateQuantity, getTotalPrice, clearCart } = useCart();
  const navigate = useNavigate();

  if (cartItems.length === 0) {
    return (
      <div className="container mx-auto px-4 py-16">
        <SEO title="Nákupní košík" noindex={true} />
        <div className="max-w-md mx-auto text-center bg-white p-8 rounded-2xl shadow-sm border border-gray-100">
          <div className="text-6xl mb-4">🛒</div>
          <h2 className="text-2xl font-bold mb-2 text-gray-800">Váš košík je prázdný</h2>
          <p className="text-gray-500 mb-6">Prozkoumejte naši nabídku a vyberte si zboží.</p>
          <Link
            to="/products"
            className="inline-block bg-blue-600 text-white font-semibold px-6 py-3 rounded-xl hover:bg-blue-700 transition-colors shadow-sm"
          >
            Přejít k nákupu
          </Link>
        </div>
      </div>
    );
  }

  const handleCheckout = () => {
    navigate('/checkout');
  };

  return (
    <div className="container mx-auto px-4 py-8 lg:py-12 max-w-6xl">
      <SEO title="Nákupní košík" noindex={true} />
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4 border-b border-gray-100 pb-6">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900">Nákupní košík</h1>
          <p className="text-gray-500 text-sm mt-1">Zkontrolujte si své vybrané položky před objednáním</p>
        </div>
        <button
          onClick={clearCart}
          className="text-red-500 hover:text-red-700 text-sm font-medium flex items-center gap-1 transition-colors"
        >
          <span>🗑️</span> Vysypat košík
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Seznam položek v košíku */}
        <div className="lg:col-span-2 space-y-4">
          {cartItems.map((item) => {
            const price = typeof item.price === 'string'
              ? Number.parseFloat(item.price)
              : item.price;
            const itemTotal = price * item.quantity;
            const imageFilename = item.mainImage || item.mainImageUrl || item.images?.[0]?.url || item.images?.[0];
            const imageUrl = getImageUrl(imageFilename);

            return (
              <div
                key={item.id}
                className="bg-white rounded-2xl p-4 md:p-6 shadow-sm border border-gray-100 flex flex-col md:flex-row items-center gap-6"
              >
                <div className="w-24 h-24 bg-gray-50 rounded-xl flex-shrink-0 flex items-center justify-center overflow-hidden border border-gray-100">
                  {imageFilename ? (
                    <img
                      src={imageUrl}
                      srcSet={getResponsiveSrcSet(imageFilename)}
                      sizes={defaultImageSizes}
                      alt={item.name}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className="text-gray-300 text-4xl">📦</div>
                  )}
                </div>

                <div className="flex-1 w-full">
                  <h3 className="text-lg font-bold text-gray-800 mb-1 line-clamp-2">{item.name}</h3>
                  <p className="text-gray-500 text-sm mb-4">
                    {price?.toFixed(2) || '0.00'} Kč za kus <span className="text-xs text-gray-400 font-normal">(vč. DPH)</span>
                  </p>

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
                  <span className="text-xs text-gray-400 block">včetně DPH</span>
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

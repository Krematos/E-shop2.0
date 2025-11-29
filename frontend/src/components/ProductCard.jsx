import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const ProductCard = ({ product }) => {
  const { addToCart } = useCart();
  const [imgError, setImgError] = useState(false);

  const handleAddToCart = (e) => {
    e.preventDefault(); // Zabrání prokliku na detail produktu při kliknutí na tlačítko
    e.stopPropagation(); // Zastaví šíření události
    addToCart(product, 1);
  };

  const rawPrice = typeof product.price === 'string'
    ? parseFloat(product.price) 
    : product.price;

  const formattedPrice = new Intl.NumberFormat('cs-CZ', {
    style: 'currency',
    currency: 'CZK',
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(rawPrice || 0);

  // Sestavení URL obrázku - pokud product.image obsahuje jen název souboru
    const imageUrl = product.image?.startsWith('http')
      ? product.image
      : `${API_BASE_URL}/images/${product.image}`;

  return (
    <div className="card group hover:shadow-xl transition-all duration-300 border border-gray-100 rounded-xl bg-white flex flex-col h-full">
          <Link to={`/products/${product.id}`} className="flex-1 flex flex-col">
            {/* Kontejner obrázku */}
            <div className="aspect-square bg-gray-50 rounded-t-xl overflow-hidden relative">
              {!imgError && product.image ? (
                <img
                  src={imageUrl}
                  alt={product.name}
                  loading="lazy" // Optimalizace výkonu
                  onError={() => setImgError(true)} // Pokud server vrátí 404, přepne na fallback
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                />
              ) : (
                // Fallback ikona, pokud obrázek chybí nebo se nenačetl
                <div className="w-full h-full flex items-center justify-center bg-gray-100 text-gray-300">
                  <span className="text-5xl">📦</span>
                </div>
              )}
            </div>

            {/* Obsah karty */}
            <div className="p-4 flex flex-col flex-1">
              <h3 className="text-lg font-bold text-gray-800 mb-2 line-clamp-2 leading-tight group-hover:text-primary-600 transition-colors">
                {product.name}
              </h3>

              <p className="text-gray-500 text-sm mb-4 line-clamp-3 flex-1">
                {product.description || "Bez popisu"}
              </p>

              <div className="mt-auto pt-4 border-t border-gray-100 flex items-center justify-between">
                <span className="text-xl font-bold text-primary-600">
                  {formattedPrice}
                </span>
              </div>
            </div>
          </Link>

          {/* Tlačítko je oddělené, ale vizuálně v kartě */}
          <div className="p-4 pt-0">
            <button
              onClick={handleAddToCart}
              className="btn-primary w-full py-2.5 rounded-lg font-medium shadow-sm hover:shadow-md active:scale-95 transition-all"
            >
              Přidat do košíku
            </button>
          </div>
        </div>
      );
};

export default ProductCard;
